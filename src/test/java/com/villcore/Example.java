package com.villcore;

import com.villcore.visitor.Visitor;
import com.villcore.visitor.VisitorAdapter;
import sun.misc.Contended;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Example {

    public static void main(String[] args) throws Exception {
        ObjectFieldHelper objectFieldHelper = new ObjectFieldHelper(() -> Contended.class);

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

        Visitor<Tag> visitor = new VisitorAdapter<Tag>() {
            @Override
            public void startVisit(Object target) {
                super.startVisit(target);
                System.out.println("Start visit");
            }

            @Override
            public void visitOnce(Object target, Field field, Tag tag, String name, Object value) {
                if (tag.klass() == String.class && value instanceof String) {
                    try {
                        field.set(target, value + "123");
                        System.out.println("=========");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.printf("object [%s] field = %s, tag = %s, name = %s, value = %s%n", System.identityHashCode(target), field.getName(), tag, name, value);
            }

            @Override
            public void completeVisit(Object target) {
                super.completeVisit(target);
                System.out.println("Complete visit");
            }
        };

        for (int i = 0; i < 1; i++) {
            long startTimeMillis = System.currentTimeMillis();
            try {
                objectFieldHelper.visit(personMap, visitor);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println(System.currentTimeMillis() - startTimeMillis);
            }
        }
        System.out.println(personMap);
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

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Tag {
        String name();
        Class<?> klass();
    }
}
