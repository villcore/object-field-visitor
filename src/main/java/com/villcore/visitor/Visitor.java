package com.villcore.visitor;

import com.villcore.annotations.Tag;

import java.lang.reflect.Field;

public interface Visitor {
    void startVisit(Object target);

    void visit(Object target, Field field, Tag tag, String name, Object value);

    void completeVisit(Object target);
}
