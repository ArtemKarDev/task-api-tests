package ru.learning.taskapitests.client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.generated.model.LoginRequest;
import ru.learning.taskapitests.utils.AllureAttachments;

import static io.restassured.RestAssured.given;


@Component
public class AuthApiClient {
    public static final String LOGIN_PATH = "/api/auth/login";
    private final TestProperties props;

    public AuthApiClient(TestProperties props) {
        this.props = props;
    }

    @Step("Войти как {username}")
    public Response login(String username, String password) {

        LoginRequest requestBody = new LoginRequest().username(username).password(password);

        return postLogin(requestBody);
    }

    @Step("Отправить «сырой» запрос на вход")
    public Response loginRaw(String body) {
        return postLogin(body);
    }

    private Response postLogin(Object body) {
        RequestSpecification requestSpec = given()
                .baseUri(props.baseUrl())
                .contentType(ContentType.JSON);

        if (body != null) {
            requestSpec.body(body);
        }

        Response response = requestSpec
                .when()
                .post(LOGIN_PATH);
        attachResponseWithoutSecrets(response);
        return response;
    }

    private void attachResponseWithoutSecrets(Response response) {
        int statusCode = response.statusCode();
        AllureAttachments.attachStatus(statusCode);
        if (statusCode != 200) {
            String responseBody = response.asPrettyString();
            if (!responseBody.isBlank()) {
                AllureAttachments.attachResponse(responseBody);
            }
        }
    }
}
