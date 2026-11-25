package ru.learning.taskapitests.db;

import java.time.LocalDateTime;

public record TaskDbRecord(
        long id,
        String title,
        String description,
        String status,
        String priority,
        String assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}