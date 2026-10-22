package ru.learning.taskapitests.models;

public record Task(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        String assignee) {

}
