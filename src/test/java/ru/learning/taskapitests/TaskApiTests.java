package ru.learning.taskapitests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class TaskApiTests {


    @Test
    void shouldCreateTaskAndGetItById(){
        String requestBody = """
                    {
                        "title": "Автотестовая задача",
                        "description": "Создана через REST Assured",
                        "status": "NEW",
                        "priority": "HIGH",
                        "assignee": "tester"
                    }
                """;
        // Создаём задачу
        int taskId =
                given()
                        .spec(ApiSpec.requestSpec())
                        .body(requestBody)
                .when()
                        .post("/api/tasks")
                .then()
                        .statusCode(201)
                        .contentType(ContentType.JSON)
                        .body("id",notNullValue())
                        .body("title",equalTo("Автотестовая задача"))
                        .body("status", equalTo("NEW"))
                        .extract()
                        .path("id");

        // Получаем созданную задачу
                given()
                        .spec(ApiSpec.requestSpec())
                .when()
                        .get("/api/tasks/{id}",taskId)
                .then()
                        .statusCode(200)
                        .body("id", equalTo(taskId))
                        .body("title", equalTo("Автотестовая задача"))
                        .body("priority",equalTo("HIGH"));
    }


    @Test
    void shouldReturn400WHenTitleIsMissing(){
        String requestBody = """
                    {
                        "description": "Задача без названия, проверка валидации обязательного поля",
                        "status": "NEW",
                        "priority": "HIGH",
                        "assignee": "tester"
                    }
                """;
        // Создаём задачу
        given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
        .when()
                .post("/api/tasks")
        .then()
                .statusCode(400);

    }

    @Test
    void shouldReturn400WHenPriorityIsInvalid(){
        String requestBody = """
                    {
                        "title": "Некорректная задача",
                        "description": "Проверка валидации значения",
                        "status": "NEW",
                        "priority": "NOT_HIGH",
                        "assignee": "tester"
                    }
                """;
        // Создаём задачу
        given()
                .spec(ApiSpec.requestSpec())
                .body(requestBody)
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(400);

    }

    @Test
    public void shouldReturn404WhenTaskDoesNotExist(){
        long nonexistentId = Long.MAX_VALUE;

        given()
                .spec(ApiSpec.requestSpec())
        .when()
                .get("/api/tasks/{id}", nonexistentId)
        .then()
                .statusCode(404)
                .body("error", equalTo("Task not found with id: "+ nonexistentId));
    }
    @Test
    public void shouldDeleteTaskAndReturn404WhenGettingIt(){
        String requestBody = """
                    {
                        "title": "Автотестовая задача",
                        "description": "Создана через REST Assured для проверки удаления",
                        "status": "NEW",
                        "priority": "HIGH",
                        "assignee": "tester"
                    }
                """;
        // Создаём задачу
        int taskId =
                given()
                        .spec(ApiSpec.requestSpec())
                        .body(requestBody)
                        .when()
                        .post("/api/tasks")
                        .then()
                        .statusCode(201)
                        .body("id",notNullValue())
                        .body("title",equalTo("Автотестовая задача"))
                        .body("status", equalTo("NEW"))
                        .extract()
                        .path("id");

        given()
                .spec(ApiSpec.requestSpec())
                .when()
                .delete("api/tasks/{id}",taskId)
                .then()
                .statusCode(204);


        given()
                .spec(ApiSpec.requestSpec())
                .when()
                .get("/api/tasks/{id}", taskId)
                .then()
                .statusCode(404);

    }

    @Test
    void shouldUpdateTask(){
        String createBody = """
            {
                "title": "Исходная задача",
                "description": "До обновления",
                "status": "NEW",
                "priority": "LOW",
                "assignee": "tester"
            }
            """;
        int taskId =
                given()
                        .spec(ApiSpec.requestSpec())
                        .body(createBody)
                .when()
                        .post("api/tasks")
                .then()
                        .statusCode(201)
                        .extract()
                        .path("id");


        String updateBody = """
            {
                "title": "Обновлённая задача",
                "description": "После обновления",
                "status": "IN_PROGRESS",
                "priority": "HIGH",
                "assignee": "senior-tester"
            }
            """;

        given()
                .spec(ApiSpec.requestSpec())
                .body(updateBody)
        .when()
                .put("/api/tasks/{id}", taskId)
        .then()
                .statusCode(200)
                .body("id",equalTo(taskId))
                .body("title",equalTo("Обновлённая задача"));

        given()
                .spec(ApiSpec.requestSpec())
        .when()
                .get("/api/tasks/{id}", taskId)
        .then()
                .statusCode(200)
                .body("title", equalTo("Обновлённая задача"));
    }

    @Test
    void shouldReturn404WhenUpdateNonexistentTask(){
        String requestBody = """
                    {
                        "title": "Автотестовая задача",
                        "description": "Создана через REST Assured для проверки удаления",
                        "status": "NEW",
                        "priority": "HIGH",
                        "assignee": "tester"
                    }
                """;
        // Создаём задачу
        int taskId =
                given()
                        .spec(ApiSpec.requestSpec())
                        .body(requestBody)
                        .when()
                        .post("/api/tasks")
                        .then()
                        .statusCode(201)
                        .body("id",notNullValue())
                        .body("title",equalTo("Автотестовая задача"))
                        .body("status", equalTo("NEW"))
                        .extract()
                        .path("id");

        given()
                .spec(ApiSpec.requestSpec())
                .when()
                .delete("api/tasks/{id}",taskId)
                .then()
                .statusCode(204);


        String updateBody = """
            {
                "title": "Несуществующая задача",
                "description": "Проверка ошибки",
                "status": "NEW",
                "priority": "LOW",
                "assignee": "tester"
            }
            """;

        given()
                .spec(ApiSpec.requestSpec())
                .body(updateBody)
                .when()
                .put("/api/tasks/{id}", taskId)
                .then()
                .statusCode(404);

    }

}
