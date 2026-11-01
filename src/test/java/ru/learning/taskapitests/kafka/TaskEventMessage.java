package ru.learning.taskapitests.kafka;

/** Событие из топика task_out. Разбор строгий: лишнее или переименованное поле считается ошибкой контракта. */
public record TaskEventMessage(
        String eventId,
        String eventType,
        String occurredAt,
        String correlationId,
        Long taskId,
        Snapshot task
) {
    public record Snapshot(
            Long id,
            String title,
            String description,
            String status,
            String priority,
            String assignee,
            String createdAt,
            String updatedAt
    ) {
    }
}