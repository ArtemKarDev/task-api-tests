package ru.learning.taskapitests.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.learning.taskapitests.client.TaskApiClient;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskSteps {

    private final TaskApiClient taskApi;

    public TaskSteps(TaskApiClient taskApi) {
        this.taskApi = taskApi;
    }

    @Step("Получить задачу по ID: {taskId}")
    public Response getTask(long taskId) {
        return taskApi.getTaskById(taskId);
    }

    @Step("Удалить задачу по ID: {taskId}")
    public Response deleteTask(long taskId) {
        return taskApi.deleteTask(taskId);
    }

    @Step("Проверить HTTP-статус: {expectedStatus}")
    public void checkStatus(Response response, int expectedStatus) {
        assertThat(response.statusCode())
                .as("HTTP-статус ответа").isEqualTo(expectedStatus);
    }

    @Step("Проверить текст ошибки {expectedError}")
    public void checkErrorText(Response response, String expectedError) {
        assertThat(response.jsonPath().getString("error"))
                .as("текст ответа c ошибкой ").isEqualTo(expectedError);
    }

    @Step("Создать задачу и получить её ID")
    public long createTask(String requestBody) {
        Response response = taskApi.createTask(requestBody);
        checkStatus(response, 201);

        Long taskId = response.jsonPath().getLong("id");

        assertThat(taskId).as("ID созданной задачи").isPositive();

        return taskId;
    }

    @Step("Проверить данные задачи")
    public void checkTaskData(Response response,
                              long expectedId,
                              String expectedTitle,
                              String expectedStatus,
                              String expectedPriority) {
        assertThat(response.jsonPath().getLong("id"))
                .as("ID задачи")
                .isEqualTo(expectedId);

        assertThat(response.jsonPath().getString("title"))
                .as("Название задачи")
                .isEqualTo(expectedTitle);

        assertThat(response.jsonPath().getString("status"))
                .as("Статус задачи")
                .isEqualTo(expectedStatus);

        assertThat(response.jsonPath().getString("priority"))
                .as("Приоритет задачи")
                .isEqualTo(expectedPriority);
    }
}
