package ru.learning.taskapitests.db;

import java.time.LocalDateTime;

public record NewDbTask(
        String title,
        String description,
        String status,
        String priority,
        String assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NewDbTask defaults(String title) {
        LocalDateTime now = LocalDateTime.now();
        return new NewDbTask(title, "Создана напрямую в БД", "NEW", "MEDIUM", null, now, now);
    }

    public NewDbTask withDates(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new NewDbTask(title, description, status, priority, assignee, createdAt, updatedAt);
    }

}
