package ru.learning.taskapitests.learning.scan;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class Welcome {

    private final Greeter greeter;
    private final Counter frozenCounter;
    private final ObjectProvider<Counter> counters;

    // Единственный конструктор: Spring сам подставит все три параметра
    public Welcome(Greeter greeter, Counter frozenCounter, ObjectProvider<Counter> counters) {
        this.greeter = greeter;
        this.frozenCounter = frozenCounter;
        this.counters = counters;
    }

    public String greet(String name) {
        return greeter.greet(name);
    }

    /** Тот же объект при каждом вызове: Spring создал его один раз, когда собирал Welcome. */
    public Counter frozenCounter() {
        return frozenCounter;
    }

    /** Новый объект при каждом вызове: мы каждый раз просим его у Spring. */
    public Counter freshCounter() {
        return counters.getObject();
    }
}