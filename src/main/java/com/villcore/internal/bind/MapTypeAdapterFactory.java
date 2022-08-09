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
import com.villcore.internal.ConstructorConstructor;
import com.villcore.internal.ObjectConstructor;
import com.villcore.reflect.TypeToken;
import com.villcore.visitor.Visitor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public final class MapTypeAdapterFactory implements TypeAdapterFactory {

    private final ConstructorConstructor constructorConstructor;

    public MapTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
        this.constructorConstructor = constructorConstructor;
    }

    @Override
    public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> typeToken) {

        Type type = typeToken.getType();
        Class<? super T> rawType = typeToken.getRawType();
        if (!Map.class.isAssignableFrom(rawType)) {
            return null;
        }

        Class<?> rawTypeOfSrc = $Gson$Types.getRawType(type);
        Type[] keyAndValueTypes = $Gson$Types.getMapKeyAndValueTypes(type, rawTypeOfSrc);
        TypeAdapter<?> keyAdapter = getKeyAdapter(objectFieldHelper, keyAndValueTypes[0]);
        TypeAdapter<?> valueAdapter = objectFieldHelper.getAdapter(TypeToken.get(keyAndValueTypes[1]));
        ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

        @SuppressWarnings({"unchecked", "rawtypes"})
        // we don't define a type parameter for the key or value types
        TypeAdapter<T> result = new Adapter(objectFieldHelper, keyAndValueTypes[0], keyAdapter,
                keyAndValueTypes[1], valueAdapter, constructor);
        return result;
    }

    /**
     * Returns a type adapter that writes the value as a string.
     */
    private TypeAdapter<?> getKeyAdapter(ObjectFieldHelper context, Type keyType) {
        return (keyType == boolean.class || keyType == Boolean.class)
                ? TypeAdapters.BOOLEAN_AS_STRING
                : context.getAdapter(TypeToken.get(keyType));
    }

    private final class Adapter<K, V> extends TypeAdapter<Map<K, V>> {
        private final TypeAdapter<K> keyTypeAdapter;
        private final TypeAdapter<V> valueTypeAdapter;
        private final ObjectConstructor<? extends Map<K, V>> constructor;

        public Adapter(ObjectFieldHelper context,
                       Type keyType, TypeAdapter<K> keyTypeAdapter,
                       Type valueType, TypeAdapter<V> valueTypeAdapter,
                       ObjectConstructor<? extends Map<K, V>> constructor) {
            this.keyTypeAdapter = new TypeAdapterRuntimeTypeWrapper<K>(context, keyTypeAdapter, keyType);
            this.valueTypeAdapter = new TypeAdapterRuntimeTypeWrapper<V>(context, valueTypeAdapter, valueType);
            this.constructor = constructor;
        }

        @Override
        public void visit(Map<K, V> map, Visitor visitor) throws IOException {
            if (map == null) {
                return;
            }

            for (Map.Entry<K, V> entry : map.entrySet()) {
                valueTypeAdapter.visit(entry.getValue(), visitor);
            }
        }
    }
}
