package ru.learning.taskapitests.specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.auth.TokenProvider;
import ru.learning.taskapitests.config.TestProperties;

import static io.restassured.http.ContentType.JSON;

@Component
public class ApiSpec {

    private final TestProperties props;
    private final TokenProvider tokenProvider;

    public ApiSpec(TestProperties props, TokenProvider tokenProvider) {
        this.props = props;
        this.tokenProvider = tokenProvider;
    }

    private RequestSpecification baseRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(props.baseUrl())
                .setContentType(JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    public RequestSpecification requestSpec() {
        return requestSpec(Role.USER);
    }

    public RequestSpecification requestSpec(Role role) {
        return requestSpecWithToken(tokenProvider.tokenFor(role));
    }


    public RequestSpecification requestSpecWithToken(String token) {
        return baseRequestSpec()
                .auth().oauth2(token);
    }

    public RequestSpecification requestSpecAnonymous() {
        return baseRequestSpec();
    }
}