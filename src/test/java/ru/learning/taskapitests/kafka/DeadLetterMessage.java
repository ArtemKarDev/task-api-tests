package ru.learning.taskapitests.kafka;

/** Сообщение из топика task_in_dlq. Разбор строгий, как и у событий. */
public record DeadLetterMessage(
        String originalValue,
        String error,
        String failedAt
) {
}