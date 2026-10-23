package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteTaskTest extends TestBase {


    @Test
    public void shouldDeleteTaskAndReturn404WhenGettingIt() {

//        int taskId = taskApi.createTask(TaskTestData.validCreateTaskRequest())
//                .then().statusCode(201).extract().path("id");

        long taskId = step.createTaskAndGetId(TaskDataFactory.validCreateTaskRequest());

        Response response = step.deleteTask(taskId);
        step.checkStatus(response, 204);

        Response getResponse = step.getTask(taskId);
        step.checkStatus(getResponse, 404);
        step.checkErrorText(getResponse,"Task not found with id: " + taskId);
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
