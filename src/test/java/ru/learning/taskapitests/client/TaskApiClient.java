package ru.learning.taskapitests.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.specs.ApiSpec;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.Task;
import ru.learning.taskapitests.utils.AllureAttachments;
import ru.learning.taskapitests.utils.JsonUtils;

import static io.restassured.RestAssured.given;

@Component
public class TaskApiClient {

    private static final String POST_TASK_CREATE = "/api/tasks";
    private static final String GET_TASK = "/api/tasks";
    private static final String BASE_PATH = "/api/tasks";
    private static final String PATCH_TASK = "/api/tasks";
    private static final String DELETE_TASK = "/api/tasks";

    private final ApiSpec apiSpec;

    public TaskApiClient(ApiSpec apiSpec) {
        this.apiSpec = apiSpec;
    }

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
                .spec(apiSpec.requestSpec())
                .body(requestBody)
                .when()
                .post(BASE_PATH);

        attachResponseDetails(response);
        return response;
    }

    @Step("Создать задачу с DTO")
    public Response createTaskDto(CreateTaskRequest request) {

        AllureAttachments.attachRequest(JsonUtils.toJson(request));

        Response response = given()
                .spec(apiSpec.requestSpec())
                .body(request)
                .when()
                .post(BASE_PATH);

        attachResponseDetails(response);

        return response;
    }


    @Step("Получить задачу по Id: : {taskId}")
    public Response getTaskById(long taskId) {

        Response response = given()
                .spec(apiSpec.requestSpec())
                .when()
                .get("/api/tasks/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Получить задачу по ID и обернуть в Task")
    public Task getTaskAsDto(long taskId) {
        Response response = given()
                .spec(apiSpec.requestSpec())
                .when()
                .get(BASE_PATH + "/{id}", taskId);

        attachResponseDetails(response);
        return response.as(Task.class);

    }

    @Step("Обновить задачу целиком по Id: : {taskId}")
    public Response updateTask(long taskId, String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(apiSpec.requestSpec())
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Обновить задачу частично по Id: : {taskId}")
    public Response patchTask(long taskId, String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(apiSpec.requestSpec())
                .body(requestBody)
                .when()
                .patch(BASE_PATH + "/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Удалить задачу по Id: : {taskId}")
    public Response deleteTask(long taskId) {
        Response response = given()
                .spec(apiSpec.requestSpec(Role.ADMIN))
                .when()
                .delete(BASE_PATH + "/{id}", taskId);

        attachResponseDetails(response);

        return response;
    }

    public Response sendRaw(String method, String path, String contentType, String body) {
        return sendRaw(Role.USER, method, path, contentType, body);
    }

    @Step("Выполнить запрос {method} {path} от имени {role}")
    public Response sendRaw(Role role, String method, String path, String contentType, String body) {
        return send(apiSpec.requestSpec(role), method, path, contentType, body);
    }

    @Step("Выполнить запрос {method} {path} без токена")
    public Response sendRawAnonymous(String method, String path, String contentType, String body) {
        return send(apiSpec.requestSpecAnonymous(), method, path, contentType, body);
    }

    @Step("Выполнить запрос {method} {path} с переданным токеном")
    public Response sendRawWithToken(String token, String method, String path, String contentType, String body) {
        return send(apiSpec.requestSpecWithToken(token), method, path, contentType, body);
    }

    private Response send(RequestSpecification spec, String method, String path, String contentType, String body) {
        if (body != null) {
            AllureAttachments.attachRequest(body);
        }

        RequestSpecification request = given().spec(spec);
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


