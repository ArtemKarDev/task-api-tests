package ru.learning.taskapitests;

import io.restassured.builder.RequestSpecBuilder;

import static io.restassured.http.ContentType.JSON;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

public class ApiSpec {

    public static RequestSpecification requestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(TestConfig.getBaseUrl())
                .setContentType(JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

}
