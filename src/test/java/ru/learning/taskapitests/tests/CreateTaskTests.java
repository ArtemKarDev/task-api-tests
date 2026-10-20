package ru.learning.taskapitests.tests;

import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;

import static org.hamcrest.Matchers.equalTo;

public class CreateTaskTests extends TestBase {


    @Test
    void shouldCreateTaskAndGetItById(){
        String requestBody = """
                {
                    "title": "Автотестовая задача",
                    "description": "Создана через REST Assured",
                    "status": "NEW",
                    "priority": "HIGH",
                    "assignee": "tester"
                }
                """;

        int taskId = taskApi.createTask(requestBody)
                .then()
                .statusCode(201)
                .body("title", equalTo("Автотестовая задача"))
                .body("status", equalTo("NEW"))
                .extract().path("id");

        taskApi.getTaskById(taskId)
                .then()
                .statusCode(200)
                .body("id",equalTo(taskId))
                .body("priority", equalTo("HIGH"));
    }
}
