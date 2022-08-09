package com.villcore.visitor;

import java.util.HashSet;
import java.util.Set;

public final class VisitorContext {

    private static final ThreadLocal<Set<Long>> VISITED_FIELDS = ThreadLocal.withInitial(() -> new HashSet<>(128));

    public static Set<Long> visitedFields() {
        return VISITED_FIELDS.get();
    }

    public static Set<Long> clearVisitedFields() {
        return VISITED_FIELDS.get();
    }
}
