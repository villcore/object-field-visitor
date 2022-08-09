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

package com.villcore.internal;

import com.villcore.*;
import com.villcore.annotations.Expose;
import com.villcore.annotations.Since;
import com.villcore.annotations.Until;
import com.villcore.reflect.TypeToken;
import com.villcore.visitor.Visitor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Excluder implements TypeAdapterFactory, Cloneable {
    private static final double IGNORE_VERSIONS = -1.0d;
    public static final Excluder DEFAULT = new Excluder();

    private double version = IGNORE_VERSIONS;
    private int modifiers = Modifier.TRANSIENT | Modifier.STATIC;
    private boolean serializeInnerClasses = true;
    private boolean requireExpose;
    private List<ExclusionStrategy> serializationStrategies = Collections.emptyList();
    private List<ExclusionStrategy> deserializationStrategies = Collections.emptyList();

    @Override
    protected Excluder clone() {
        try {
            return (Excluder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public Excluder withVersion(double ignoreVersionsAfter) {
        Excluder result = clone();
        result.version = ignoreVersionsAfter;
        return result;
    }

    public Excluder withModifiers(int... modifiers) {
        Excluder result = clone();
        result.modifiers = 0;
        for (int modifier : modifiers) {
            result.modifiers |= modifier;
        }
        return result;
    }

    public Excluder disableInnerClassSerialization() {
        Excluder result = clone();
        result.serializeInnerClasses = false;
        return result;
    }

    public Excluder excludeFieldsWithoutExposeAnnotation() {
        Excluder result = clone();
        result.requireExpose = true;
        return result;
    }

    public Excluder withExclusionStrategy(ExclusionStrategy exclusionStrategy,
                                          boolean serialization, boolean deserialization) {
        Excluder result = clone();
        if (serialization) {
            result.serializationStrategies = new ArrayList<ExclusionStrategy>(serializationStrategies);
            result.serializationStrategies.add(exclusionStrategy);
        }
        if (deserialization) {
            result.deserializationStrategies
                    = new ArrayList<ExclusionStrategy>(deserializationStrategies);
            result.deserializationStrategies.add(exclusionStrategy);
        }
        return result;
    }

    public <T> TypeAdapter<T> create(final ObjectFieldHelper objectFieldHelper, final TypeToken<T> type) {
        Class<?> rawType = type.getRawType();
        final boolean skipSerialize = excludeClass(rawType, true);
        final boolean skipDeserialize = excludeClass(rawType, false);

        if (!skipSerialize && !skipDeserialize) {
            return null;
        }

        return new TypeAdapter<T>() {
            /** The delegate is lazily created because it may not be needed, and creating it may fail. */
            private TypeAdapter<T> delegate;

            @Override
            public void visit(T value, Visitor visitor) throws IOException {
                if (skipSerialize) {
                    return;
                }
                delegate().visit(value, visitor);
            }

            private TypeAdapter<T> delegate() {
                TypeAdapter<T> d = delegate;
                return d != null
                        ? d
                        : (delegate = objectFieldHelper.getDelegateAdapter(Excluder.this, type));
            }
        };
    }

    public boolean excludeField(Field field, boolean serialize) {
        if ((modifiers & field.getModifiers()) != 0) {
            return true;
        }

        if (version != Excluder.IGNORE_VERSIONS
                && !isValidVersion(field.getAnnotation(Since.class), field.getAnnotation(Until.class))) {
            return true;
        }

        if (field.isSynthetic()) {
            return true;
        }

        if (requireExpose) {
            Expose annotation = field.getAnnotation(Expose.class);
            if (annotation == null || (serialize ? !annotation.serialize() : !annotation.deserialize())) {
                return true;
            }
        }

        if (!serializeInnerClasses && isInnerClass(field.getType())) {
            return true;
        }

        if (isAnonymousOrLocal(field.getType())) {
            return true;
        }

        List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
        if (!list.isEmpty()) {
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            for (ExclusionStrategy exclusionStrategy : list) {
                if (exclusionStrategy.shouldSkipField(fieldAttributes)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean excludeClass(Class<?> clazz, boolean serialize) {
        if (version != Excluder.IGNORE_VERSIONS
                && !isValidVersion(clazz.getAnnotation(Since.class), clazz.getAnnotation(Until.class))) {
            return true;
        }

        if (!serializeInnerClasses && isInnerClass(clazz)) {
            return true;
        }

        if (isAnonymousOrLocal(clazz)) {
            return true;
        }

        List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
        for (ExclusionStrategy exclusionStrategy : list) {
            if (exclusionStrategy.shouldSkipClass(clazz)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAnonymousOrLocal(Class<?> clazz) {
        return !Enum.class.isAssignableFrom(clazz)
                && (clazz.isAnonymousClass() || clazz.isLocalClass());
    }

    private boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !isStatic(clazz);
    }

    private boolean isStatic(Class<?> clazz) {
        return (clazz.getModifiers() & Modifier.STATIC) != 0;
    }

    private boolean isValidVersion(Since since, Until until) {
        return isValidSince(since) && isValidUntil(until);
    }

    private boolean isValidSince(Since annotation) {
        if (annotation != null) {
            double annotationVersion = annotation.value();
            if (annotationVersion > version) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidUntil(Until annotation) {
        if (annotation != null) {
            double annotationVersion = annotation.value();
            if (annotationVersion <= version) {
                return false;
            }
        }
        return true;
    }
}
