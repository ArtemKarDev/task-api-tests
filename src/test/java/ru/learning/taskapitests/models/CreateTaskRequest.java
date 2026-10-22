package ru.learning.taskapitests.models;

public record CreateTaskRequest(
        String title,
        String description,
        String status,
        String priority,
        String assignee) {

}
