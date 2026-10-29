package ru.learning.taskapitests.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.learning.taskapitests.specs.ApiSpec;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.Task;
import ru.learning.taskapitests.utils.AllureAttachments;
import ru.learning.taskapitests.utils.JsonUtils;

import static io.restassured.RestAssured.given;

public class TaskApiClient {

    private static final String POST_TASK_CREATE = "/api/tasks";
    private static final String GET_TASK = "/api/tasks";
    private static final String UPDATE_TASK = "/api/tasks";
    private static final String PATCH_TASK = "/api/tasks";
    private static final String DELETE_TASK = "/api/tasks";

    private void attachResponseDetails(Response response) {
        AllureAttachments.attachStatus(response.statusCode());

        String responseBody = response.asPrettyString();
        if (!responseBody.isBlank()) {
            AllureAttachments.attachResponse(responseBody);
        }
    }

    @Step("Создать задачу")
    public Response createTask(String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .post(POST_TASK_CREATE);

        attachResponseDetails(response);
        return response;
    }

    @Step("Создать задачу с DTO")
    public Response createTaskDto(CreateTaskRequest request) {

        AllureAttachments.attachRequest(JsonUtils.toJson(request));

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .body(request)
                .when()
                .post(POST_TASK_CREATE);

        attachResponseDetails(response);

//        log.info("\n************ Статус код: {}\n", response.statusCode());
//        log.info("\n************ Body: {}\n", response.body().asPrettyString());

        return response;
    }


    @Step("Получить задачу по Id: : {taskId}")
    public Response getTaskById(long taskId) {

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .when()
                .get("/api/tasks/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Получить задачу по ID и обернуть в Task")
    public Task getTaskAsDto(long taskId) {
        Response response = given()
                .spec(ApiSpec.requestSpec())
                .when()
                .get(GET_TASK + "/{id}", taskId);

        attachResponseDetails(response);
        return response.as(Task.class);

    }

    @Step("Обновить задачу целиком по Id: : {taskId}")
    public Response updateTask(long taskId, String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .put("/api/tasks/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Обновить задачу частично по Id: : {taskId}")
    public Response patchTask(long taskId, String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .patch("/api/tasks/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Удалить задачу по Id: : {taskId}")
    public Response deleteTask(long taskId) {
        Response response = given()
                .spec(ApiSpec.requestSpec())
                .when()
                .delete("/api/tasks/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Выполнить запрос {method} {path}")
    public Response sendRaw(String method, String path, String contentType, String body) {
        if (body != null) {
            AllureAttachments.attachRequest(body);
        }

        RequestSpecification request = given().spec(ApiSpec.requestSpec());
        if (contentType != null) {
            request.contentType(contentType);
        }
        if (body != null) {
            request.body(body);
        }

        Response response = request.when().request(method, path);
        attachResponseDetails(response);
        return response;
    }

}


