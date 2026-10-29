package ru.learning.taskapitests.db;

public record TaskDbRecord(
        long id,
        String title,
        String description,
        String status,
        String priority,
        String assignee
) {
}