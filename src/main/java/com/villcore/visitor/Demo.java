package com.villcore.visitor;

import com.villcore.ObjectFieldHelper;
import com.villcore.annotations.Tag;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Demo {
    public static void main(String[] args) throws Exception {
        ObjectFieldHelper objectFieldHelper = new ObjectFieldHelper();

        Person parent = new Person();
        parent.name = "A";
        parent.age = 22;
        parent.parent = null;
        parent.wordList = Arrays.asList("111111", "222222");

        Person p = new Person();
        p.name = "alice";
        p.age = 12;
        p.parent = parent;
        p.date = new Date();

        List<Person> personList = Arrays.asList(p, parent);
        Map<String, List<Person>> personMap = personList.stream().collect(Collectors.toMap(person -> person.name, Collections::singletonList));

        ThreadLocal<Set<Long>> visitedField = ThreadLocal.withInitial(() -> new HashSet<>(128));

        Visitor visitor = new Visitor() {
            @Override
            public void startVisit(Object target) {
                System.out.println("Start visit");
            }

            @Override
            public void visit(Object target, Field field, Tag tag, String name, Object value) {
                // ThreadLocal
                if (visitedField.get().add(objectFieldIdentity(target, field))) {
                    // 屏蔽重复设置
                    if (tag.klass() == String.class && value instanceof String) {
                        try {
                            field.set(target, "123");
                            System.out.println("=========");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.printf("object [%s] field = %s, tag = %s, name = %s, value = %s%n", System.identityHashCode(target), field.getName(), tag, name, value);
                } else {
                    System.out.println("find duplicate target field");
                }
            }

            @Override
            public void completeVisit(Object target) {
                System.out.println("Complete visit");
            }
        };

        for (int i = 0; i < 100; i++) {
            long startTimeMillis = System.currentTimeMillis();
            try {
                objectFieldHelper.visit(personMap, visitor);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                visitedField.get().clear();
                System.out.println(System.currentTimeMillis() - startTimeMillis);
            }
        }
        System.out.println(personMap);
    }

    public static Long objectFieldIdentity(Object target, Field field) {
        int objectId = System.identityHashCode(target);
        int fieldId = System.identityHashCode(field);
        long objectFieldId = 0L;
        objectFieldId = objectFieldId | ((long) objectId << 32);
        objectFieldId = objectFieldId | (long) fieldId;
        return objectFieldId;
    }

    static class Person {
        @Tag(name = "p_name", klass = String.class)
        private String name;

        // @Tag(name = "p_age")
        private int age;

        // @Tag(name = "p_parent")
        private Person parent;

        // @Tag(name = "p_date")
        private Date date;

        @Tag(name = "p_words", klass = List.class)
        private List<String> wordList;

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", parent=" + parent +
                    ", date=" + date +
                    ", wordList=" + wordList +
                    '}';
        }
    }
}
