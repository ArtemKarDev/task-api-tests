package ru.learning.taskapitests.tests.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;

@Tag(Tags.REGRESSION)
@Tag(Tags.API)
@DisplayName("Запросы удаленя задач")
public class DeleteTaskTest extends TestBase {

    @Tag(Tags.SMOKE)
    @DisplayName("Запрос удаления задачи по ID и её получения")
    @Test
    public void shouldDeleteTaskAndReturn404WhenGettingIt() {

        long taskId = step.createTaskAndGetId(TaskDataFactory.validCreateTaskRequest());

        Response response = step.deleteTask(taskId);
        step.checkStatus(response, 204);

        Response getResponse = step.getTask(taskId);
        step.checkApiError(getResponse,404, "Task not found with id: " + taskId, "/api/tasks/" + taskId);

    }

    @Test
    @Tag(Tags.NEGATIVE)
    public void shouldReturn404WhenDeleteNonexistentTask() {

        long taskId = Long.MAX_VALUE;
        Response response = taskApi.deleteTask(taskId);
        step.checkApiError(response,404, "Task not found with id: " + taskId, "/api/tasks/" + taskId);


    }

}
