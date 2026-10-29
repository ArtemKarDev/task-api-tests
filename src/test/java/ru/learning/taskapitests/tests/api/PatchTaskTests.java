package ru.learning.taskapitests.tests.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.REGRESSION)
@Tag(Tags.API)
@DisplayName("Запросы частичного обновления задач")
public class PatchTaskTests extends TestBase {
    @Tag(Tags.SMOKE)
    @DisplayName("Запрос на обновление атриута задачи")
    @Test
    void shouldPatchOnlyPriorityAndKeepOtherFields() {
        long taskId = step.createTaskAndGetId(TaskDataFactory.validCreateTaskRequest());

        Response patchResponse = taskApi.patchTask(taskId, TaskDataFactory.validPatchPriorityRequestBody());
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
    @Tag(Tags.NEGATIVE)
    void shouldReturn404WhenNonexistentTask() {
        long taskId = Long.MAX_VALUE;
        Response patchResponse = taskApi.patchTask(taskId, TaskDataFactory.validPatchPriorityRequestBody());
        step.checkApiError(patchResponse, 404, "Task not found with id: " + taskId, "/api/tasks/" + taskId);
    }

    @Test
    @Tag(Tags.NEGATIVE)
    void shouldReturn400WhenPatchBodyIsEmpty() {

        taskApi.patchTask(1, TaskDataFactory.emptyRequestBody()).then().statusCode(400);

    }


}
