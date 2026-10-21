package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskTestData;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CreateTaskTests extends TestBase {


    @Test
    void shouldCreateTaskAndGetItById() {

        Response createResponse = taskApi.createTask(TaskTestData.validCreateTaskRequest());

        assertThat(createResponse.statusCode())
                .as("HTTP-статус при создании задачи")
                .isEqualTo(201);


        Long taskId = createResponse.jsonPath().getLong("id");

        registerTaskForCleanup(taskId);

        assertThat(taskId).as("ID созданной задачи").isPositive();
        assertThat(createResponse.jsonPath().getString("title")).as("Название созданной задачи").isEqualTo("Автотестовая задача");
        assertThat(createResponse.jsonPath().getString("status")).as("Статус созданной задачи").isEqualTo("NEW");
        assertThat(createResponse.jsonPath().getString("priority")).as("Приоритет  созданной задачи").isEqualTo("HIGH");

        Response getResponse = taskApi.getTaskById(taskId);

        assertThat(getResponse.statusCode())
                .as("HTTP-статус при получении задачи по ID")
                .isEqualTo(200);

        assertThat(getResponse.jsonPath().getLong("id")).as("ID полученной задачи").isEqualTo(taskId);
        assertThat(getResponse.jsonPath().getString("title")).as("Название полученной задачи").isEqualTo("Автотестовая задача");
        assertThat(getResponse.jsonPath().getString("priority")).as("Приоритет полученной задачи").isEqualTo("HIGH");

    }

    @Test
    void shouldReturn400WhenTitleIsMissing() {
        Response response = taskApi.createTask(TaskTestData.invalidCreateTaskRequestWithoutTitle());

        assertThat(response.statusCode()).as("HTTP-статус при создании задачи без названия").isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenPriorityIsInvalid() {
        Response response = taskApi.createTask(TaskTestData.invalidCreateTaskRequestInvalidPriority());

        assertThat(response.statusCode()).as("HTTP-статус при создании задачи с некорректным приоритетом").isEqualTo(400);
    }


}
