/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.villcore;

import com.villcore.internal.bind.*;
import com.villcore.reflect.TypeToken;
import com.villcore.visitor.Visitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public final class ObjectFieldHelper {

    private static final TypeToken<?> NULL_KEY_SURROGATE = TypeToken.get(Object.class);

    private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls = new ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>>();

    private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache = new ConcurrentHashMap<TypeToken<?>, TypeAdapter<?>>();

    private final List<TypeAdapterFactory> factories;

    private final Annotation annotation;

    public ObjectFieldHelper(Annotation annotation) {
        this.annotation = annotation;
        List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>();

        factories.add(ObjectTypeAdapter.FACTORY);
        // type adapters for basic platform types
        factories.add(TypeAdapters.STRING_FACTORY);
        factories.add(TypeAdapters.INTEGER_FACTORY);
        // factories.add(TypeAdapters.BOOLEAN_FACTORY);
        // factories.add(TypeAdapters.BYTE_FACTORY);
        // factories.add(TypeAdapters.SHORT_FACTORY);
        factories.add(TypeAdapters.newFactory(long.class, Long.class, TypeAdapters.LONG));
        factories.add(TypeAdapters.newFactory(double.class, Double.class, doubleAdapter()));
        factories.add(TypeAdapters.newFactory(float.class, Float.class, floatAdapter(false)));
        factories.add(TypeAdapters.NUMBER_FACTORY);
        // factories.add(TypeAdapters.ATOMIC_INTEGER_FACTORY);
        // factories.add(TypeAdapters.ATOMIC_BOOLEAN_FACTORY);
        // factories.add(TypeAdapters.newFactory(AtomicLong.class, atomicLongAdapter(longAdapter)));
        // factories.add(TypeAdapters.newFactory(AtomicLongArray.class, atomicLongArrayAdapter(longAdapter)));
        // factories.add(TypeAdapters.ATOMIC_INTEGER_ARRAY_FACTORY);
        // factories.add(TypeAdapters.CHARACTER_FACTORY);
        // factories.add(TypeAdapters.STRING_BUILDER_FACTORY);
        // factories.add(TypeAdapters.STRING_BUFFER_FACTORY);
        // factories.add(TypeAdapters.newFactory(BigDecimal.class, TypeAdapters.BIG_DECIMAL));
        // factories.add(TypeAdapters.newFactory(BigInteger.class, TypeAdapters.BIG_INTEGER));
        // factories.add(TypeAdapters.URL_FACTORY);
        // factories.add(TypeAdapters.URI_FACTORY);
        // factories.add(TypeAdapters.UUID_FACTORY);
        // factories.add(TypeAdapters.CURRENCY_FACTORY);
        // factories.add(TypeAdapters.LOCALE_FACTORY);
        // factories.add(TypeAdapters.INET_ADDRESS_FACTORY);
        // factories.add(TypeAdapters.BIT_SET_FACTORY);
        factories.add(DateTypeAdapter.FACTORY);
        // factories.add(TypeAdapters.CALENDAR_FACTORY);
        // factories.add(TimeTypeAdapter.FACTORY);
        // factories.add(SqlDateTypeAdapter.FACTORY);
        // factories.add(TypeAdapters.TIMESTAMP_FACTORY);
        // factories.add(ArrayTypeAdapter.FACTORY);
        // factories.add(TypeAdapters.CLASS_FACTORY);

        // type adapters for composite and user-defined types
        factories.add(new CollectionTypeAdapterFactory());
        factories.add(new MapTypeAdapterFactory());
        // factories.add(TypeAdapters.ENUM_FACTORY);
        factories.add(new ReflectiveTypeAdapterFactory());

        this.factories = Collections.unmodifiableList(factories);
    }

    private TypeAdapter<Number> doubleAdapter() {
        if (false) {
            return TypeAdapters.DOUBLE;
        }
        return new TypeAdapter<Number>() {
            @Override
            public void visit(Number value, Visitor visitor) throws Exception {
                if (value == null) {
                    return;
                }
                double doubleValue = value.doubleValue();
                checkValidFloatingPoint(doubleValue);
            }
        };
    }

    private TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
        if (serializeSpecialFloatingPointValues) {
            return TypeAdapters.FLOAT;
        }
        return new TypeAdapter<Number>() {
            @Override
            public void visit(Number value, Visitor visitor) throws Exception {
                if (value == null) {
                    return;
                }
                float floatValue = value.floatValue();
                checkValidFloatingPoint(floatValue);
            }
        };
    }

    static void checkValidFloatingPoint(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(value
                    + " is not a valid double value as per JSON specification. To override this"
                    + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
        }
    }

    private static TypeAdapter<AtomicLong> atomicLongAdapter(final TypeAdapter<Number> longAdapter) {
        return new TypeAdapter<AtomicLong>() {
            @Override
            public void visit(AtomicLong value, Visitor visitor) throws Exception {
                longAdapter.visit(value.get(), visitor);
            }
        }.nullSafe();
    }

    private static TypeAdapter<AtomicLongArray> atomicLongArrayAdapter(final TypeAdapter<Number> longAdapter) {
        return new TypeAdapter<AtomicLongArray>() {
            @Override
            public void visit(AtomicLongArray value, Visitor visitor) throws Exception {
                for (int i = 0, length = value.length(); i < length; i++) {
                    longAdapter.visit(value.get(i), visitor);
                }
            }
        }.nullSafe();
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
        TypeAdapter<?> cached = typeTokenCache.get(type == null ? NULL_KEY_SURROGATE : type);
        if (cached != null) {
            return (TypeAdapter<T>) cached;
        }

        Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = calls.get();
        boolean requiresThreadLocalCleanup = false;
        if (threadCalls == null) {
            threadCalls = new HashMap<TypeToken<?>, FutureTypeAdapter<?>>();
            calls.set(threadCalls);
            requiresThreadLocalCleanup = true;
        }

        // the key and value type parameters always agree
        FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter<T>) threadCalls.get(type);
        if (ongoingCall != null) {
            return ongoingCall;
        }

        try {
            FutureTypeAdapter<T> call = new FutureTypeAdapter<T>();
            threadCalls.put(type, call);

            for (TypeAdapterFactory factory : factories) {
                TypeAdapter<T> candidate = factory.create(this, type);
                if (candidate != null) {
                    call.setDelegate(candidate);
                    typeTokenCache.put(type, candidate);
                    return candidate;
                }
            }
            throw new IllegalArgumentException("GSON cannot handle " + type);
        } finally {
            threadCalls.remove(type);

            if (requiresThreadLocalCleanup) {
                calls.remove();
            }
        }
    }

    public <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
        throw new IllegalArgumentException("ObjectFieldHelper cannot handle " + type);
    }

    public <T> TypeAdapter<T> getAdapter(Class<T> type) {
        return getAdapter(TypeToken.get(type));
    }

    public <T extends Annotation> void visit(Object src, Visitor<T> visitor) throws Exception {
        visitor.startVisit(src);
        if (src != null) {
            visit(src, src.getClass(), visitor);
        }
        visitor.completeVisit(src);
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    @SuppressWarnings("unchecked")
    public void visit(Object src, Type typeOfSrc, Visitor visitor) throws Exception {
        TypeAdapter<?> adapter = getAdapter(TypeToken.get(typeOfSrc));
        ((TypeAdapter<Object>) adapter).visit(src, visitor);
    }

    static class FutureTypeAdapter<T> extends TypeAdapter<T> {
        private TypeAdapter<T> delegate;

        public void setDelegate(TypeAdapter<T> typeAdapter) {
            if (delegate != null) {
                throw new AssertionError();
            }
            delegate = typeAdapter;
        }

        @Override
        public void visit(T value, Visitor visitor) throws Exception {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            delegate.visit(value, visitor);
        }
    }
}
