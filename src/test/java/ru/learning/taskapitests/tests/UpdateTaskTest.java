package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskTestData;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateTaskTest extends TestBase {


    @Test
    void shouldUpdateTask() {

        long taskId = createTaskForTest(TaskTestData.validCreateTaskRequest());

        Response updateResponse = taskApi.updateTask(taskId, TaskTestData.validUpdateRequestBody());

        steps.checkStatus(updateResponse, 200);
        steps.checkTaskData(updateResponse,taskId,"Обновлённая задача","IN_PROGRESS","MEDIUM");

        Response getResponse = steps.getTask(taskId);
        steps.checkStatus(getResponse, 200);
        steps.checkTaskData(getResponse,taskId,"Обновлённая задача","IN_PROGRESS","MEDIUM");

    }

    @Test
    void shouldReturn404WhenUpdateNonexistentTask() {

        long taskId = Long.MAX_VALUE;

        Response updateResponse = taskApi.updateTask(taskId, TaskTestData.validUpdateRequestBody());

        steps.checkStatus(updateResponse, 404);
        steps.checkErrorText(updateResponse,"Task not found with id: " + taskId);
    }

}
