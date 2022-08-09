package com.villcore.visitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface Visitor<T extends Annotation> {
    void startVisit(Object target);

    void visit(Object target, Field field, T annotation, String name, Object value);

    void completeVisit(Object target);
}
