package ru.learning.taskapitests.tests.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
@Tag(Tags.REGRESSION)
@Tag(Tags.API)
@DisplayName("Запросы получения задач")
public class GetTaskTests extends TestBase {

    @Test
    @Tag(Tags.NEGATIVE)
    public void shouldReturn404WhenTaskDoesNotExist() {
        long nonexistentId = Long.MAX_VALUE;

        Response response = step.getTask(nonexistentId);
        step.checkStatus(response, 404);
        step.checkErrorText(response,"Task not found with id: " + nonexistentId);

    }

}
