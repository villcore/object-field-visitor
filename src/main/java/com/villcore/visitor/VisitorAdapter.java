package com.villcore.visitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public abstract class VisitorAdapter<T extends Annotation> implements Visitor<T> {

    @Override
    public void startVisit(Object target) {
        VisitorContext.clearVisitedFields();
    }

    @Override
    public void visit(Object target, Field field, T annotation, String name, Object value) {
        Long objectFieldIdentity = objectFieldIdentity(target, field);
        if (VisitorContext.visitedFields().add(objectFieldIdentity)) {
            visitOnce(target, field, annotation, name, value);
        }
    }

    public abstract void visitOnce(Object target, Field field, T annotation, String name, Object value);

    @Override
    public void completeVisit(Object target) {
        VisitorContext.clearVisitedFields();
    }

    public static Long objectFieldIdentity(Object target, Field field) {
        int objectId = System.identityHashCode(target);
        int fieldId = System.identityHashCode(field);
        long objectFieldId = 0L;
        objectFieldId = objectFieldId | ((long) objectId << 32);
        objectFieldId = objectFieldId | (long) fieldId;
        return objectFieldId;
    }
}
