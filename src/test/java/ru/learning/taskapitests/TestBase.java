package ru.learning.taskapitests;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.data.TaskTestData;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.steps.TaskSteps;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;

public class TestBase {

    protected TaskApiClient taskApi;
    protected TaskSteps steps;
    private final List<Long> createdTaskIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        taskApi = new TaskApiClient();
        steps = new TaskSteps(taskApi);
        createdTaskIds.clear();
    }

//    protected long createdTask() {
//        Response response = taskApi.createTask(TaskTestData.validCreateTaskRequest());
//        response.then()
//                .statusCode(201)
//                .body("id", notNullValue());
//        Long taskId = response.jsonPath().getLong("id");
//        createdTaskIds.add(taskId);
//
//        return taskId;
//    }
    protected long createTaskForTest(String requestBody) {
        long taskId = steps.createTask(requestBody);
        registerTaskForCleanup(taskId);

        return taskId;
    }

    protected Response createDefaultTaskWithDto(CreateTaskRequest request){

        Response response = steps.createDefaultTaskWithDto(request);
        Long taskId = response.jsonPath().getLong("id");
        registerTaskForCleanup(taskId);

        return response;

    }

        @AfterEach
    protected void cleanUp() {
        System.out.println("Очистка бд: " + createdTaskIds.toString());
        for (Long taskId : createdTaskIds) {
            int statusCode =
                    taskApi.deleteTask(taskId).then().extract().statusCode();
            if (statusCode != 204 && statusCode != 404) {
                throw new AssertionError("Неудалось удалить задачу с ID " + taskId + " HTTP-статус: " + statusCode);
            }
        }
        createdTaskIds.clear();
    }

    protected void registerTaskForCleanup(Long taskId) {
        createdTaskIds.add(taskId);
    }


}
