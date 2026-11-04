package ru.learning.taskapitests.specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;

import static io.restassured.http.ContentType.JSON;

@Component
public class ApiSpec {

    private final TestProperties props;

    public ApiSpec(TestProperties props) {
        this.props = props;
    }

    public RequestSpecification requestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(props.baseUrl())
                .setContentType(JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

}