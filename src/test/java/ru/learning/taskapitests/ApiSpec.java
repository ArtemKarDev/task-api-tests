package ru.learning.taskapitests;

import io.restassured.builder.RequestSpecBuilder;

import static io.restassured.http.ContentType.JSON;

import io.restassured.specification.RequestSpecification;

public class ApiSpec {

    public static RequestSpecification requestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(TestConfig.getBaseUrl())
                .setContentType(JSON)
                .build();
    }

}
