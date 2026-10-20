package ru.learning.taskapitests.client;

import io.restassured.response.Response;
import ru.learning.taskapitests.ApiSpec;

import static io.restassured.RestAssured.given;

public class TaskApiClient {

    public Response createTask(String requestBody){
        return given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .post("/api/tasks");
    }

    public Response getTaskById(long taskId){
        return given()
                .spec(ApiSpec.requestSpec())
                .when()
                .get("/api/tasks/{id}", taskId);
    }

    public Response updateTask(long taskId, String requestBody){
        return given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .put("/api/tasks/{id}", taskId);
    }

    public Response patchTask(long taskId, String requestBody){
        return given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .patch("/api/test/{id}", taskId);
    }

    public Response deleteTask(long taskId){
        return given()
                .spec(ApiSpec.requestSpec())
                .when()
                .delete("/api/tasks/{id}", taskId);
    }

}
