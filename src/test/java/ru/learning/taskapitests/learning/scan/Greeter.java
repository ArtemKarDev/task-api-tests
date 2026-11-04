package ru.learning.taskapitests.learning.scan;

import org.springframework.stereotype.Component;

/** Без состояния: одного экземпляра хватает всем. Область по умолчанию, singleton. */
@Component
public class Greeter {

    public String greet(String name) {
        return "Hello, " + name;
    }
}