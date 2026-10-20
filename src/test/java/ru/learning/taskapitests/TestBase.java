package ru.learning.taskapitests;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.data.TaskTestData;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;

public class TestBase {

    protected TaskApiClient taskApi;
    private final List<Long> createdTaskIds = new ArrayList<>();

    @BeforeEach
    void setUp(){
        taskApi = new TaskApiClient();
        createdTaskIds.clear();
    }

    protected long createdTask(){
        Response response = taskApi.createTask(TaskTestData.validCreateTaskRequest());

        response.then()
                .statusCode(201)
                .body("id", notNullValue());
        Long taskId = response.jsonPath().getLong("id");
        createdTaskIds.add(taskId);

        return taskId;
    }

    @AfterEach
    protected void cleanUp(){
        System.out.println("Очистка бд: "+ createdTaskIds.toString());
        for (Long taskId : createdTaskIds){
            taskApi.deleteTask(taskId).then().statusCode(204);
        }
    createdTaskIds.clear();
    }
}
