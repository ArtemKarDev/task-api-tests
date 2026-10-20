package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskTestData;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CreateTaskTests extends TestBase {


    @Test
    void shouldCreateTaskAndGetItById() {

        Response createResponse = taskApi.createTask(TaskTestData.validCreateTaskRequest());

        createResponse.then().statusCode(201);

        int taskId = createResponse.jsonPath().getInt("id");

        assertThat(taskId).as("ID созданной задачи").isPositive();
        assertThat(createResponse.jsonPath().getString("title")).as("Название созданной задачи").isEqualTo("Автотестовая задача");
        assertThat(createResponse.jsonPath().getString("status")).as("Статус созданной задачи").isEqualTo("NEW");
        assertThat(createResponse.jsonPath().getString("priority")).as("Приоритет  созданной задачи").isEqualTo("HIGH");

        Response getResponse =taskApi.getTaskById(taskId);

        getResponse.then().statusCode(200);

        assertThat(getResponse.jsonPath().getInt("id")).as("ID полученной задачи").isEqualTo(taskId);
        assertThat(getResponse.jsonPath().getString("title")).as("Название полученной задачи").isEqualTo("Автотестовая задача");
        assertThat(getResponse.jsonPath().getString("priority")).as("Приоритет полученной задачи").isEqualTo("HIGH");

    }
}
