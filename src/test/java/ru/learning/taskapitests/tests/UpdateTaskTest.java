package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskTestData;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateTaskTest extends TestBase {


    @Test
    void shouldUpdateTask() {

        long taskId = createdTask();

        Response updateResponse = taskApi.updateTask(taskId, TaskTestData.validUpdateRequestBody());

        updateResponse.then().statusCode(200);
        assertThat(updateResponse.jsonPath().getString("title"))
                .as("ПРоверка обновления title")
                .isEqualTo("Обновлённая задача");


        Response getResponse = taskApi.getTaskById(taskId);
        getResponse.then().statusCode(200);
        assertThat(getResponse.jsonPath().getString("title"))
                .as("Проверка обновления title в get запросе")
                .isEqualTo("Обновлённая задача");

    }

    @Test
    void shouldReturn404WhenUpdateNonexistentTask() {

        long taskId = Long.MAX_VALUE;

        Response updateResponse = taskApi.updateTask(taskId, TaskTestData.validUpdateRequestBody());
        updateResponse.then().statusCode(404);

        assertThat(updateResponse.jsonPath().getString("error"))
                .as("Текст ошибки")
                .isEqualTo("Task not found with id: " + taskId);
    }

}
