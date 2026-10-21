package ru.learning.taskapitests.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.learning.taskapitests.ApiSpec;
import ru.learning.taskapitests.utils.AllureAttachments;

import static io.restassured.RestAssured.given;

public class TaskApiClient {

    @Step("Создать задачу")
    public Response createTask(String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .post("/api/tasks");

        AllureAttachments.attachStatus(response.statusCode());
        AllureAttachments.attachResponse(response.asPrettyString());

        return response;
    }

    @Step("Получить задачу по Id: : {taskId}")
    public Response getTaskById(long taskId) {

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .when()
                .get("/api/tasks/{id}", taskId);

        AllureAttachments.attachStatus(response.statusCode());
        AllureAttachments.attachResponse(response.asPrettyString());

        return response;
    }

    @Step("Обновить задачу целиком по Id: : {taskId}")
    public Response updateTask(long taskId, String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .put("/api/tasks/{id}", taskId);

        AllureAttachments.attachStatus(response.statusCode());
        AllureAttachments.attachResponse(response.asPrettyString());

        return response;
    }

    @Step("Обновить задачу частично по Id: : {taskId}")
    public Response patchTask(long taskId, String requestBody) {

        AllureAttachments.attachRequest(requestBody);

        Response response = given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .patch("/api/test/{id}", taskId);

        AllureAttachments.attachStatus(response.statusCode());
        AllureAttachments.attachResponse(response.asPrettyString());

        return response;
    }

    @Step("Удалить задачи по Id: : {taskId}")
    public Response deleteTask(long taskId) {
        Response response = given()
                .spec(ApiSpec.requestSpec())
                .when()
                .delete("/api/tasks/{id}", taskId);

        AllureAttachments.attachStatus(response.statusCode());
        AllureAttachments.attachResponse(response.asPrettyString());

        return response;
    }

}
