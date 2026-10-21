package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;

public class GetTaskTests extends TestBase {

    @Test
    public void shouldReturn404WhenTaskDoesNotExist() {
        long nonexistentId = Long.MAX_VALUE;

        Response response = steps.getTask(nonexistentId);
        steps.checkStatus(response, 404);
        steps.checkErrorText(response,"Task not found with id: " + nonexistentId);

    }

}
