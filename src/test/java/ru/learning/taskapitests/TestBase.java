package ru.learning.taskapitests;

import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import ru.learning.taskapitests.client.TaskApiClient;

import static org.hamcrest.Matchers.notNullValue;

public class TestBase {

    protected TaskApiClient taskApi;

    @BeforeEach
    void setUp(){
        taskApi = new TaskApiClient();
    }

    protected long createdTask(){
        String requestBody = """
                {
                    "title": "Автотестовая задача",
                    "description": "Создана через REST Assured",
                    "status": "NEW",
                    "priority": "HIGH",
                    "assignee": "tester"
                }
                """;
        Response response = taskApi.createTask(requestBody);

        response.then().statusCode(201)
                .body("id", notNullValue());

        return response.jsonPath().getLong("id");
    }
}
