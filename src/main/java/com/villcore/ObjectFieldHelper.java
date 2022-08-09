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

import com.villcore.internal.ConstructorConstructor;
import com.villcore.internal.Excluder;
import com.villcore.reflect.TypeToken;
import com.villcore.stream.JsonWriter;
import com.villcore.internal.bind.*;
import com.villcore.visitor.Visitor;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public final class ObjectFieldHelper {
    static final boolean DEFAULT_JSON_NON_EXECUTABLE = false;
    static final boolean DEFAULT_LENIENT = false;
    static final boolean DEFAULT_PRETTY_PRINT = false;
    static final boolean DEFAULT_ESCAPE_HTML = true;
    static final boolean DEFAULT_SERIALIZE_NULLS = false;
    static final boolean DEFAULT_COMPLEX_MAP_KEYS = false;
    static final boolean DEFAULT_SPECIALIZE_FLOAT_VALUES = false;

    private static final TypeToken<?> NULL_KEY_SURROGATE = TypeToken.get(Object.class);

    private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls = new ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>>();

    private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache = new ConcurrentHashMap<TypeToken<?>, TypeAdapter<?>>();

    private final List<TypeAdapterFactory> factories;
    private final ConstructorConstructor constructorConstructor;

    private final boolean serializeNulls;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;

    public ObjectFieldHelper() {
        this(Excluder.DEFAULT, FieldNamingPolicy.IDENTITY,
                Collections.<Type, InstanceCreator<?>>emptyMap(), DEFAULT_SERIALIZE_NULLS,
                DEFAULT_COMPLEX_MAP_KEYS, DEFAULT_JSON_NON_EXECUTABLE, DEFAULT_ESCAPE_HTML,
                DEFAULT_PRETTY_PRINT, DEFAULT_LENIENT, DEFAULT_SPECIALIZE_FLOAT_VALUES,
                LongSerializationPolicy.DEFAULT, Collections.<TypeAdapterFactory>emptyList());
    }

    ObjectFieldHelper(final Excluder excluder, final FieldNamingStrategy fieldNamingStrategy,
                      final Map<Type, InstanceCreator<?>> instanceCreators, boolean serializeNulls,
                      boolean complexMapKeySerialization, boolean generateNonExecutableGson, boolean htmlSafe,
                      boolean prettyPrinting, boolean lenient, boolean serializeSpecialFloatingPointValues,
                      LongSerializationPolicy longSerializationPolicy,
                      List<TypeAdapterFactory> typeAdapterFactories) {
        this.constructorConstructor = new ConstructorConstructor(instanceCreators);
        this.serializeNulls = serializeNulls;

        List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>();

        factories.add(ObjectTypeAdapter.FACTORY);
        factories.add(excluder);
        factories.addAll(typeAdapterFactories);

        // type adapters for basic platform types
        factories.add(TypeAdapters.STRING_FACTORY);
        factories.add(TypeAdapters.INTEGER_FACTORY);
        // factories.add(TypeAdapters.BOOLEAN_FACTORY);
        // factories.add(TypeAdapters.BYTE_FACTORY);
        // factories.add(TypeAdapters.SHORT_FACTORY);
        TypeAdapter<Number> longAdapter = longAdapter(longSerializationPolicy);
        factories.add(TypeAdapters.newFactory(long.class, Long.class, longAdapter));
        factories.add(TypeAdapters.newFactory(double.class, Double.class, doubleAdapter(serializeSpecialFloatingPointValues)));
        factories.add(TypeAdapters.newFactory(float.class, Float.class, floatAdapter(serializeSpecialFloatingPointValues)));
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
        factories.add(new CollectionTypeAdapterFactory(constructorConstructor));
        factories.add(new MapTypeAdapterFactory(constructorConstructor));
        this.jsonAdapterFactory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        factories.add(jsonAdapterFactory);
        // factories.add(TypeAdapters.ENUM_FACTORY);
        factories.add(new ReflectiveTypeAdapterFactory(constructorConstructor, fieldNamingStrategy, excluder, jsonAdapterFactory));

        this.factories = Collections.unmodifiableList(factories);
    }

    private TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
        if (serializeSpecialFloatingPointValues) {
            return TypeAdapters.DOUBLE;
        }
        return new TypeAdapter<Number>() {
            @Override
            public void visit(Number value, Visitor visitor) throws IOException {
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
            public void visit(Number value, Visitor visitor) throws IOException {
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

    private static TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy) {
        if (longSerializationPolicy == LongSerializationPolicy.DEFAULT) {
            return TypeAdapters.LONG;
        }
        return new TypeAdapter<Number>() {
            @Override
            public void visit(Number value, Visitor visitor) throws IOException {
                if (value == null) {
                    return;
                }
            }
        };
    }

    private static TypeAdapter<AtomicLong> atomicLongAdapter(final TypeAdapter<Number> longAdapter) {
        return new TypeAdapter<AtomicLong>() {
            @Override
            public void visit(AtomicLong value, Visitor visitor) throws IOException {
                longAdapter.visit(value.get(), visitor);
            }
        }.nullSafe();
    }

    private static TypeAdapter<AtomicLongArray> atomicLongArrayAdapter(final TypeAdapter<Number> longAdapter) {
        return new TypeAdapter<AtomicLongArray>() {
            @Override
            public void visit(AtomicLongArray value, Visitor visitor) throws IOException {
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
        // Hack. If the skipPast factory isn't registered, assume the factory is being requested via
        // our @JsonAdapter annotation.
        if (!factories.contains(skipPast)) {
            skipPast = jsonAdapterFactory;
        }

        boolean skipPastFound = false;
        for (TypeAdapterFactory factory : factories) {
            if (!skipPastFound) {
                if (factory == skipPast) {
                    skipPastFound = true;
                }
                continue;
            }

            TypeAdapter<T> candidate = factory.create(this, type);
            if (candidate != null) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("GSON cannot serialize " + type);
    }

    public <T> TypeAdapter<T> getAdapter(Class<T> type) {
        return getAdapter(TypeToken.get(type));
    }

    public void visit(Object src, Visitor visitor) throws Exception {
        visitor.startVisit(src);
        if (src != null) {
            visit(src, src.getClass(), visitor);
        }
        visitor.completeVisit(src);
    }

    private static final Writer dummyWriter = new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {

        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }
    };
    private static final JsonWriter dummyJsonWriter = new JsonWriter(dummyWriter);

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
        public void visit(T value, Visitor visitor) throws IOException {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            delegate.visit(value, visitor);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("{serializeNulls:")
                .append(serializeNulls)
                .append(",factories:").append(factories)
                .append(",instanceCreators:").append(constructorConstructor)
                .append("}")
                .toString();
    }
}
