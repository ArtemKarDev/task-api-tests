package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;

import static org.assertj.core.api.Assertions.assertThat;
@Tag("regress")
@Tag("api_tests")
public class UpdateTaskTest extends TestBase {

    @Tag("smoke")
    @DisplayName("Запрос на обновление всех атрибутов задачи")
    @Test
    void shouldUpdateTask() {

        long taskId = step.createTaskAndGetId(TaskDataFactory.validCreateTaskRequest());

        Response updateResponse = taskApi.updateTask(taskId, TaskDataFactory.validUpdateRequestBody());

        step.checkStatus(updateResponse, 200);
        step.checkTaskData(updateResponse,taskId,"Обновлённая задача","IN_PROGRESS","MEDIUM");

        Response getResponse = step.getTask(taskId);
        step.checkStatus(getResponse, 200);
        step.checkTaskData(getResponse,taskId,"Обновлённая задача","IN_PROGRESS","MEDIUM");

    }

    @Test
    @DisplayName("Запрос на обновление несуществующей задачи")
    @Tag("negative")
    void shouldReturn404WhenUpdateNonexistentTask() {

        long taskId = Long.MAX_VALUE;

        Response updateResponse = taskApi.updateTask(taskId, TaskDataFactory.validUpdateRequestBody());

        step.checkStatus(updateResponse, 404);
        step.checkErrorText(updateResponse,"Task not found with id: " + taskId);
    }

}
