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
import com.villcore.reflect.TypeToken;
import com.villcore.visitor.Visitor;

import java.io.IOException;

/**
 * Adapts types whose static type is only 'Object'. Uses getClass() on
 * serialization and a primitive/Map/List on deserialization.
 */
public final class ObjectTypeAdapter extends TypeAdapter<Object> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(ObjectFieldHelper objectFieldHelper, TypeToken<T> type) {
            if (type.getRawType() == Object.class) {
                return (TypeAdapter<T>) new ObjectTypeAdapter(objectFieldHelper);
            }
            return null;
        }
    };

    private final ObjectFieldHelper objectFieldHelper;

    ObjectTypeAdapter(ObjectFieldHelper objectFieldHelper) {
        this.objectFieldHelper = objectFieldHelper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Object value, Visitor visitor) throws IOException {
        if (value == null) {
            return;
        }

        TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) objectFieldHelper.getAdapter(value.getClass());
        if (typeAdapter instanceof ObjectTypeAdapter) {
            return;
        }

        typeAdapter.visit(value, visitor);
    }
}
