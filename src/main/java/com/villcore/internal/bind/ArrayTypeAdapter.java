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
import com.villcore.reflect.TypeToken;
import com.villcore.visitor.Visitor;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * Adapt an array of objects.
 */
public final class ArrayTypeAdapter<E> extends TypeAdapter<Object> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
            Type type = typeToken.getType();
            if (!(type instanceof GenericArrayType || type instanceof Class && ((Class<?>) type).isArray())) {
                return null;
            }

            Type componentType = $Gson$Types.getArrayComponentType(type);
            TypeAdapter<?> componentTypeAdapter = objectFieldHelper.getAdapter(TypeToken.get(componentType));
            return new ArrayTypeAdapter(
                    objectFieldHelper, componentTypeAdapter, $Gson$Types.getRawType(componentType));
        }
    };

    private final Class<E> componentType;
    private final TypeAdapter<E> componentTypeAdapter;

    public ArrayTypeAdapter(ObjectFieldHelper context, TypeAdapter<E> componentTypeAdapter, Class<E> componentType) {
        this.componentTypeAdapter =
                new TypeAdapterRuntimeTypeWrapper<E>(context, componentTypeAdapter, componentType);
        this.componentType = componentType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Object array, Visitor visitor) throws Exception {
        if (array == null) {
            return;
        }

        for (int i = 0, length = Array.getLength(array); i < length; i++) {
            E value = (E) Array.get(array, i);
            componentTypeAdapter.visit(value,visitor);
        }
    }
}
