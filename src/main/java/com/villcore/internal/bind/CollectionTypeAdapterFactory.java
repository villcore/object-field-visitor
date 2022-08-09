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

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class CollectionTypeAdapterFactory implements TypeAdapterFactory {

    public CollectionTypeAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {
        Type type = typeToken.getType();

        Class<? super T> rawType = typeToken.getRawType();
        if (!Collection.class.isAssignableFrom(rawType)) {
            return null;
        }

        Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
        TypeAdapter<?> elementTypeAdapter = objectFieldHelper.getAdapter(TypeToken.get(elementType));

        @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
        TypeAdapter<T> result = new Adapter(objectFieldHelper, elementType, elementTypeAdapter);
        return result;
    }

    private static final class Adapter<E> extends TypeAdapter<Collection<E>> {
        private final TypeAdapter<E> elementTypeAdapter;

        public Adapter(ObjectFieldHelper context, Type elementType,
                       TypeAdapter<E> elementTypeAdapter) {
            this.elementTypeAdapter = new TypeAdapterRuntimeTypeWrapper<E>(context, elementTypeAdapter, elementType);
        }

        @Override
        public void visit(Collection<E> collection, Visitor visitor) throws Exception {
            if (collection == null) {
                return;
            }

            for (E element : collection) {
                elementTypeAdapter.visit(element, visitor);
            }
        }
    }
}
