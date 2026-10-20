package ru.learning.taskapitests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class TaskApiTests {

    private static final String BASE_URL = "http://localhost:8080";

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
                        .baseUri(BASE_URL)
                        .contentType(ContentType.JSON)
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
                        .baseUri(BASE_URL)
                .when()
                        .get("/api/tasks/{id}",taskId)
                .then()
                        .statusCode(200)
                        .body("id", equalTo(taskId))
                        .body("title", equalTo("Автотестовая задача"))
                        .body("priority",equalTo("HIGH"));
    }

}
