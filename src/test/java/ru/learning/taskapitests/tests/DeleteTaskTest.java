package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskTestData;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteTaskTest extends TestBase {


    @Test
    public void shouldDeleteTaskAndReturn404WhenGettingIt() {

//        int taskId = taskApi.createTask(TaskTestData.validCreateTaskRequest())
//                .then().statusCode(201).extract().path("id");

        long taskId = createTaskForTest(TaskTestData.validCreateTaskRequest());

        Response response = steps.deleteTask(taskId);
        steps.checkStatus(response, 204);

        Response getResponse = steps.getTask(taskId);
        steps.checkStatus(getResponse, 404);
        steps.checkErrorText(response,"Task not found with id: " + taskId);
    }

    @Test
    public void shouldReturn404WhenDeleteNonexistentTask(){

        long taskId = Long.MAX_VALUE;
        Response response = taskApi.deleteTask(taskId);
        response.then().statusCode(404);

        assertThat(response.jsonPath().getString("error"))
                .as("Текст ошибки")
                .isEqualTo("Task not found with id: " + taskId);
    }

}
