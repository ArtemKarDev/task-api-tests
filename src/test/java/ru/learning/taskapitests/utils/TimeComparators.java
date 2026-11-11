package ru.learning.taskapitests.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

public final class TimeComparators {

    private TimeComparators() {}

    /**
     * Сравнивает даты-строки с допуском в 1 мс.
     * Нужно, потому что БД хранит микросекунды, а сервис при создании может вернуть больше знаков.
     */
    public static final Comparator<String> SAME_MOMENT = (a, b) -> {
        LocalDateTime first = LocalDateTime.parse(a);
        LocalDateTime second = LocalDateTime.parse(b);
        return Duration.between(first, second).abs().compareTo(Duration.ofMillis(1)) < 0
                ? 0
                : first.compareTo(second);
    };
}