package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class GetTaskTests extends TestBase {

    @Test
    public void shouldReturn404WhenTaskDoesNotExist() {
        long nonexistentId = Long.MAX_VALUE;

        Response response = taskApi.getTaskById(nonexistentId);

        response.then().statusCode(404);
        assertThat(response.jsonPath().getString("error"))
                .as("Текст ошибки")
                .isEqualTo("Task not found with id: " + nonexistentId);
    }

}
