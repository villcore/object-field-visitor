/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.villcore.internal.bind;

import com.villcore.ObjectFieldHelper;
import com.villcore.TypeAdapter;
import com.villcore.TypeAdapterFactory;
import com.villcore.annotations.SerializedName;
import com.villcore.reflect.TypeToken;
import com.villcore.visitor.Visitor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Type adapters for basic types.
 */
public final class TypeAdapters {
    private TypeAdapters() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    public static final TypeAdapter<Class> CLASS = new TypeAdapter<Class>() {
        @Override
        public void visit(Class value, Visitor visitor) throws IOException {
            throw new UnsupportedOperationException("Attempted to serialize java.lang.Class: "
                    + value.getName() + ". Forgot to register a type adapter?");
        }
    }.nullSafe();

    public static final TypeAdapterFactory CLASS_FACTORY = newFactory(Class.class, CLASS);

    public static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter<BitSet>() {
        @Override
        public void visit(BitSet src, Visitor visitor) throws IOException {
            for (int i = 0, length = src.length(); i < length; i++) {
                int value = (src.get(i)) ? 1 : 0;
                // out.value(value);
            }
        }
    }.nullSafe();

    public static final TypeAdapterFactory BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);

    public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {

        @Override
        public void visit(Boolean value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    /**
     * Writes a boolean as a string. Useful for map keys, where booleans aren't
     * otherwise permitted.
     */
    public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING = new TypeAdapter<Boolean>() {

        @Override
        public void visit(Boolean value, Visitor visitor) throws IOException {
            // out.value(value == null ? "null" : value.toString());
        }
    };

    public static final TypeAdapterFactory BOOLEAN_FACTORY = newFactory(boolean.class, Boolean.class, BOOLEAN);

    public static final TypeAdapter<Number> BYTE = new TypeAdapter<Number>() {

        @Override
        public void visit(Number value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapterFactory BYTE_FACTORY = newFactory(byte.class, Byte.class, BYTE);

    public static final TypeAdapter<Number> SHORT = new TypeAdapter<Number>() {
        @Override
        public void visit(Number value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapterFactory SHORT_FACTORY = newFactory(short.class, Short.class, SHORT);

    public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {

        @Override
        public void visit(Number value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapterFactory INTEGER_FACTORY = newFactory(int.class, Integer.class, INTEGER);

    public static final TypeAdapter<AtomicInteger> ATOMIC_INTEGER = new TypeAdapter<AtomicInteger>() {
        @Override
        public void visit(AtomicInteger value, Visitor visitor) throws IOException {
            // out.value(value.get());
        }
    }.nullSafe();

    public static final TypeAdapterFactory ATOMIC_INTEGER_FACTORY = newFactory(AtomicInteger.class, TypeAdapters.ATOMIC_INTEGER);

    public static final TypeAdapter<AtomicBoolean> ATOMIC_BOOLEAN = new TypeAdapter<AtomicBoolean>() {
        @Override
        public void visit(AtomicBoolean value, Visitor visitor) throws IOException {
            // out.value(value.get());
        }
    }.nullSafe();

    public static final TypeAdapterFactory ATOMIC_BOOLEAN_FACTORY = newFactory(AtomicBoolean.class, TypeAdapters.ATOMIC_BOOLEAN);

    public static final TypeAdapter<AtomicIntegerArray> ATOMIC_INTEGER_ARRAY = new TypeAdapter<AtomicIntegerArray>() {

        @Override
        public void visit(AtomicIntegerArray value, Visitor visitor) throws IOException {
            for (int i = 0, length = value.length(); i < length; i++) {
                // out.value(value.get(i));
            }
        }
    }.nullSafe();

    public static final TypeAdapterFactory ATOMIC_INTEGER_ARRAY_FACTORY = newFactory(AtomicIntegerArray.class, TypeAdapters.ATOMIC_INTEGER_ARRAY);

    public static final TypeAdapter<Number> LONG = new TypeAdapter<Number>() {

        @Override
        public void visit(Number value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapter<Number> FLOAT = new TypeAdapter<Number>() {

        @Override
        public void visit(Number value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapter<Number> DOUBLE = new TypeAdapter<Number>() {

        @Override
        public void visit(Number value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapter<Number> NUMBER = new TypeAdapter<Number>() {

        @Override
        public void visit(Number value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapterFactory NUMBER_FACTORY = newFactory(Number.class, NUMBER);

    public static final TypeAdapter<Character> CHARACTER = new TypeAdapter<Character>() {

        @Override
        public void visit(Character value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : String.valueOf(value));
        }
    };

    public static final TypeAdapterFactory CHARACTER_FACTORY = newFactory(char.class, Character.class, CHARACTER);

    public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {

        @Override
        public void visit(String value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapter<BigDecimal> BIG_DECIMAL = new TypeAdapter<BigDecimal>() {

        @Override
        public void visit(BigDecimal value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapter<BigInteger> BIG_INTEGER = new TypeAdapter<BigInteger>() {

        @Override
        public void visit(BigInteger value, Visitor visitor) throws IOException {
            // out.value(value);
        }
    };

    public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, STRING);

    public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter<StringBuilder>() {

        @Override
        public void visit(StringBuilder value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : value.toString());
        }
    };

    public static final TypeAdapterFactory STRING_BUILDER_FACTORY = newFactory(StringBuilder.class, STRING_BUILDER);

    public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter<StringBuffer>() {

        @Override
        public void visit(StringBuffer value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : value.toString());
        }
    };

    public static final TypeAdapterFactory STRING_BUFFER_FACTORY = newFactory(StringBuffer.class, STRING_BUFFER);

    public static final TypeAdapter<URL> URL = new TypeAdapter<URL>() {

        @Override
        public void visit(URL value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : value.toExternalForm());
        }
    };

    public static final TypeAdapterFactory URL_FACTORY = newFactory(URL.class, URL);

    public static final TypeAdapter<URI> URI = new TypeAdapter<URI>() {

        @Override
        public void visit(URI value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : value.toASCIIString());
        }
    };

    public static final TypeAdapterFactory URI_FACTORY = newFactory(URI.class, URI);

    public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter<InetAddress>() {

        @Override
        public void visit(InetAddress value, Visitor visitor) throws IOException {
            //out.value(value == null ? null : value.getHostAddress());
        }
    };

    public static final TypeAdapterFactory INET_ADDRESS_FACTORY =
            newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);

    public static final TypeAdapter<UUID> UUID = new TypeAdapter<UUID>() {

        @Override
        public void visit(UUID value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : value.toString());
        }
    };

    public static final TypeAdapterFactory UUID_FACTORY = newFactory(UUID.class, UUID);

    public static final TypeAdapter<Currency> CURRENCY = new TypeAdapter<Currency>() {

        @Override
        public void visit(Currency value, Visitor visitor) throws IOException {
            // out.value(value.getCurrencyCode());
        }
    }.nullSafe();
    public static final TypeAdapterFactory CURRENCY_FACTORY = newFactory(Currency.class, CURRENCY);

    public static final TypeAdapterFactory TIMESTAMP_FACTORY = new TypeAdapterFactory() {

        @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
        @Override
        public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
            if (typeToken.getRawType() != Timestamp.class) {
                return null;
            }

            final TypeAdapter<Date> dateTypeAdapter = objectFieldHelper.getAdapter(Date.class);
            return (TypeAdapter<T>) new TypeAdapter<Timestamp>() {

                @Override
                public void visit(Timestamp value, Visitor visitor) throws IOException {
                    dateTypeAdapter.visit(value, visitor);
                }
            };
        }
    };

    public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter<Calendar>() {
        private static final String YEAR = "year";
        private static final String MONTH = "month";
        private static final String DAY_OF_MONTH = "dayOfMonth";
        private static final String HOUR_OF_DAY = "hourOfDay";
        private static final String MINUTE = "minute";
        private static final String SECOND = "second";

        @Override
        public void visit(Calendar value, Visitor visitor) throws IOException {
            if (value == null) {
                // out.nullValue();
                return;
            }
            // out.beginObject();
            // out.name(YEAR);
            // out.value(value.get(Calendar.YEAR));
            // out.name(MONTH);
            // out.value(value.get(Calendar.MONTH));
            // out.name(DAY_OF_MONTH);
            // out.value(value.get(Calendar.DAY_OF_MONTH));
            // out.name(HOUR_OF_DAY);
            // out.value(value.get(Calendar.HOUR_OF_DAY));
            // out.name(MINUTE);
            // out.value(value.get(Calendar.MINUTE));
            // out.name(SECOND);
            // out.value(value.get(Calendar.SECOND));
            // out.endObject();
        }
    };

    public static final TypeAdapterFactory CALENDAR_FACTORY = newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);

    public static final TypeAdapter<Locale> LOCALE = new TypeAdapter<Locale>() {

        @Override
        public void visit(Locale value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : value.toString());
        }
    };

    public static final TypeAdapterFactory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);

    private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
        private final Map<String, T> nameToConstant = new HashMap<String, T>();
        private final Map<T, String> constantToName = new HashMap<T, String>();

        public EnumTypeAdapter(Class<T> classOfT) {
            try {
                for (T constant : classOfT.getEnumConstants()) {
                    String name = constant.name();
                    SerializedName annotation = classOfT.getField(name).getAnnotation(SerializedName.class);
                    if (annotation != null) {
                        name = annotation.value();
                        for (String alternate : annotation.alternate()) {
                            nameToConstant.put(alternate, constant);
                        }
                    }
                    nameToConstant.put(name, constant);
                    constantToName.put(constant, name);
                }
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public void visit(T value, Visitor visitor) throws IOException {
            // out.value(value == null ? null : constantToName.get(value));
        }
    }

    public static final TypeAdapterFactory ENUM_FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
            Class<? super T> rawType = typeToken.getRawType();
            if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
                return null;
            }
            if (!rawType.isEnum()) {
                rawType = rawType.getSuperclass(); // handle anonymous subclasses
            }
            return (TypeAdapter<T>) new EnumTypeAdapter(rawType);
        }
    };

    public static <TT> TypeAdapterFactory newFactory(final TypeToken<TT> type, final TypeAdapter<TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            @Override
            public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
                return typeToken.equals(type) ? (TypeAdapter<T>) typeAdapter : null;
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(final Class<TT> type, final TypeAdapter<TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            @Override
            public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
                return typeToken.getRawType() == type ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[type=" + type.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(final Class<TT> unboxed, final Class<TT> boxed, final TypeAdapter<? super TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            @Override
            public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                return (rawType == unboxed || rawType == boxed) ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[type=" + boxed.getName()
                        + "+" + unboxed.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactoryForMultipleTypes(final Class<TT> base,
                                                                     final Class<? extends TT> sub, final TypeAdapter<? super TT> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            @Override
            public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                return (rawType == base || rawType == sub) ? (TypeAdapter<T>) typeAdapter : null;
            }

            @Override
            public String toString() {
                return "Factory[type=" + base.getName()
                        + "+" + sub.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    /**
     * Returns a factory for all subtypes of {@code typeAdapter}. We do a runtime check to confirm
     * that the deserialized type matches the type requested.
     */
    public static <T1> TypeAdapterFactory newTypeHierarchyFactory(final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T2> TypeAdapter<T2> create(ObjectFieldHelper objectFieldHelper, TypeToken<T2> typeToken) {
                final Class<? super T2> requestedType = typeToken.getRawType();
                if (!clazz.isAssignableFrom(requestedType)) {
                    return null;
                }
                return (TypeAdapter<T2>) new TypeAdapter<T1>() {
                    @Override
                    public void visit(T1 value, Visitor visitor) throws IOException {
                        typeAdapter.visit(value, visitor);
                    }

                };
            }

            @Override
            public String toString() {
                return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }
}