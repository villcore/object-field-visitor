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
import com.villcore.internal.$Gson$Types;
import com.villcore.internal.Primitives;
import com.villcore.reflect.TypeToken;
import com.villcore.visitor.Visitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {

    public ReflectiveTypeAdapterFactory() {}

    /**
     * first element holds the default name
     */

    @Override
    public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, final TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();

        if (!Object.class.isAssignableFrom(raw)) {
            return null; // it's a primitive!
        }

        return new Adapter<T>(getBoundFields(objectFieldHelper, type, raw));
    }

    private BoundField createBoundField(
            final ObjectFieldHelper context, final Field field, final String name,
            final TypeToken<?> fieldType) {

        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        // special casing primitives here saves ~5% on Android...
        Annotation annotation = context.getAnnotation();
        Annotation tagAnnotation = field.getAnnotation(annotation.annotationType());
        final TypeAdapter<?> typeAdapter = context.getAdapter(fieldType);

        return new BoundField(field, name, tagAnnotation) {

            @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
            @Override
            void visit(Object value, Visitor visitor) throws Exception {
                Object fieldValue = field.get(value);
                TypeAdapter t = new TypeAdapterRuntimeTypeWrapper(context, typeAdapter, fieldType.getType());
                if (fieldValue != null) {
                    t.visit(fieldValue, visitor);
                }
                visitor.visit(value, this.field, this.tag, this.name, fieldValue);
            }

            @Override
            public boolean visitable(Object value) {
                return Objects.nonNull(tag);
            }
        };
    }

    private Map<String, BoundField> getBoundFields(ObjectFieldHelper context, TypeToken<?> type, Class<?> raw) {
        Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
        if (raw.isInterface()) {
            return result;
        }

        Type declaredType = type.getType();
        while (raw != Object.class) {
            Field[] fields = raw.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
                List<String> fieldNames = Collections.singletonList(field.getName());
                BoundField previous = null;
                for (int i = 0, size = fieldNames.size(); i < size; ++i) {
                    String name = fieldNames.get(i);
                    BoundField boundField = createBoundField(context, field, name, TypeToken.get(fieldType));
                    BoundField replaced = result.put(name, boundField);
                    if (previous == null) previous = replaced;
                }
                if (previous != null) {
                    throw new IllegalArgumentException(declaredType
                            + " declares multiple JSON fields named " + previous.name);
                }
            }
            type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }

    static abstract class BoundField {
        final Field field;
        final String name;
        final Annotation tag;

        protected BoundField(Field field, String name, Annotation tag) {
            this.field = field;
            this.name = name;
            this.tag = tag;
        }

        abstract boolean visitable(Object value);

        abstract void visit(Object value, Visitor visitor) throws Exception;
    }

    public static final class Adapter<T> extends TypeAdapter<T> {
        private final Map<String, BoundField> boundFields;

        Adapter(Map<String, BoundField> boundFields) {
            this.boundFields = boundFields;
        }

        @Override
        public void visit(T value, Visitor visitor) throws Exception {
            if (value == null) {
                return;
            }

            try {
                for (BoundField boundField : boundFields.values()) {
                    if (boundField.visitable(value)) {
                        boundField.visit(value, visitor);
                    }
                }
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
    }
}
