package ru.learning.taskapitests.data;

public final class TaskTestData {
    private TaskTestData() {

    }

    public static String validCreateTaskRequest() {
        return """
                {
                    "title": "Автотестовая задача",
                    "description": "Создана через REST Assured",
                    "status": "NEW",
                    "priority": "HIGH",
                    "assignee": "tester"
                }
                """;
    }


}
