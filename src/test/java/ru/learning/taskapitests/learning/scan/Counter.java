package ru.learning.taskapitests.learning.scan;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Хранит состояние (счётчик): общий на всех счётчик был бы ошибкой, поэтому prototype. */
@Component
@Scope("prototype")
public class Counter {

    private int value;

    public int next() {
        return ++value;
    }
}