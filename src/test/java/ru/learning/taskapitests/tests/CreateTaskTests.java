package ru.learning.taskapitests.tests;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskTestData;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CreateTaskTests extends TestBase {


    @Test
    void shouldCreateTaskAndGetItById() {

        Response createResponse = taskApi.createTask(TaskTestData.validCreateTaskRequest());
        steps.checkStatus(createResponse, 201);

        long taskId = createResponse.jsonPath().getLong("id");

        registerTaskForCleanup(taskId);

        steps.checkTaskData(createResponse, taskId,"Автотестовая задача","NEW" , "HIGH");

        Response getResponse = steps.getTask(taskId);
        steps.checkStatus(getResponse, 200);
        steps.checkTaskData(getResponse, taskId,"Автотестовая задача","NEW" , "HIGH");

    }

    @Test
    void shouldReturn400WhenTitleIsMissing() {
        Response response = taskApi.createTask(TaskTestData.invalidCreateTaskRequestWithoutTitle());
        steps.checkStatus(response, 400);
    }

    @Test
    void shouldReturn400WhenPriorityIsInvalid() {
        Response response = taskApi.createTask(TaskTestData.invalidCreateTaskRequestInvalidPriority());

        assertThat(response.statusCode()).as("HTTP-статус при создании задачи с некорректным приоритетом").isEqualTo(400);
    }


}
