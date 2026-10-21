package ru.learning.taskapitests.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.learning.taskapitests.ApiSpec;
import ru.learning.taskapitests.utils.AllureAttachments;

import static io.restassured.RestAssured.given;

public class TaskApiClient {

    private void attachResponseDetails(Response response){
        AllureAttachments.attachStatus(response.statusCode());

        String responseBody = response.asPrettyString();
        if (!responseBody.isBlank()){
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
                .post("/api/tasks");

        attachResponseDetails(response);

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


}
