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

    public static String invalidCreateTaskRequestWithoutTitle() {
        return """
                    {
                        "description": "Задача без названия, проверка валидации обязательного поля",
                        "status": "NEW",
                        "priority": "HIGH",
                        "assignee": "tester"
                    }
                """;
    }

    public static String invalidCreateTaskRequestInvalidPriority() {
        return """
                    {
                        "title": "Некорректная задача",
                        "description": "Проверка валидации значения",
                        "status": "NEW",
                        "priority": "NOT_HIGH",
                        "assignee": "tester"
                    }
                """;
    }

    public static String validUpdateRequestBody() {
        return """
                {
                    "title": "Обновлённая задача",
                    "description": "После обновления",
                    "status": "IN_PROGRESS",
                    "priority": "MEDIUM",
                    "assignee": "senior-tester"
                }
                """;
    }

    public static String validPatchPriorityRequestBody() {
        return """
                {
                    "priority": "LOW"
                }
                """;
    }

    public static String emptyRequestBody(){
        return "{}";
    }

}
