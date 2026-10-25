package ru.learning.taskapitests.data;

import ru.learning.taskapitests.generated.model.CreateTaskRequest;

import java.util.UUID;
import java.util.function.Consumer;

public final class TaskDataFactory {
    private TaskDataFactory() {

    }

    public static CreateTaskRequest defaultTaskRequestDTO() {
        return new CreateTaskRequest()
                .title(uniqueTitle("Автотест"))
                .description("Создана через REST Assured")
                .status(CreateTaskRequest.StatusEnum.NEW)
                .priority(CreateTaskRequest.PriorityEnum.MEDIUM)
                .assignee("tester");
    }

    public static CreateTaskRequest customTaskRequestDTO(Consumer<CreateTaskRequest> customizer) {
        var request = defaultTaskRequestDTO();
        customizer.accept(request);
        return request;
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

    public static String emptyRequestBody() {
        return "{}";
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String uniqueTitle(String prefix) {
        return prefix + " " + uniqueSuffix();
    }

}
