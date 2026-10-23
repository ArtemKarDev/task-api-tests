package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
@Tag("regress")
@Tag("api_tests")
public class GetTaskTests extends TestBase {

    @Test
    @Tag("negative")
    public void shouldReturn404WhenTaskDoesNotExist() {
        long nonexistentId = Long.MAX_VALUE;

        Response response = step.getTask(nonexistentId);
        step.checkStatus(response, 404);
        step.checkErrorText(response,"Task not found with id: " + nonexistentId);

    }

}
