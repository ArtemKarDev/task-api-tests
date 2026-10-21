package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskTestData;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchTaskTests extends TestBase {

    @Test
    void shouldPatchOnlyPriorityAndKeepOtherFields() {
        long taskId = createTaskForTest(TaskTestData.validCreateTaskRequest());

        Response patchResponse = taskApi.patchTask(taskId, TaskTestData.validPatchPriorityRequestBody());
        patchResponse.then().statusCode(200);

        assertThat(patchResponse.jsonPath().getString("title"))
                .as("Пповерка обновления title  в patch ответе")
                .isEqualTo("Автотестовая задача");

        assertThat(patchResponse.jsonPath().getString("priority"))
                .as("Проверка обновления priority в patch ответе")
                .isEqualTo("LOW");

        Response getResponse = taskApi.getTaskById(taskId);
        getResponse.then().statusCode(200);
        assertThat(getResponse.jsonPath().getString("title"))
                .as("Проверка обновления title в get ответе")
                .isEqualTo("Автотестовая задача");

        assertThat(getResponse.jsonPath().getString("priority"))
                .as("Проверка обновления priority в get твете")
                .isEqualTo("LOW");

    }

    @Test
    void shouldReturn404WhenNonexistentTask() {

        long taskId = Long.MAX_VALUE;

        Response patchResponse = taskApi.patchTask(taskId, TaskTestData.validPatchPriorityRequestBody());
        patchResponse.then().statusCode(404);

        assertThat(patchResponse.jsonPath().getString("error"))
                .as("Текст ошибки")
                .isEqualTo("Task not found with id: " + taskId);
    }

    @Test
    void shouldReturn400WhenPatchBodyIsEmpty() {

        taskApi.patchTask(1,TaskTestData.emptyRequestBody()).then().statusCode(400);

    }


}
