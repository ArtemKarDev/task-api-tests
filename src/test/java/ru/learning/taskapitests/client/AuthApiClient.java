package ru.learning.taskapitests.client;

import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.generated.model.LoginRequest;
import ru.learning.taskapitests.generated.model.TokenResponse;
import ru.learning.taskapitests.utils.AllureAttachments;

import java.util.Map;

import static io.restassured.RestAssured.given;


@Component
public class AuthApiClient  {
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

    @Step("Войти как {username} и получить токен")
    public String loginAndGetToken(String username, String password) {
        Response response = login(username, password);
        response.then().statusCode(200);
        return response.as(TokenResponse.class).getAccessToken();
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

    @Step("Сменить свой пароль с токеном пользователя")
    public Response changeOwnPassword(@Param(mode = Parameter.Mode.HIDDEN) String token,
                                      @Param(mode = Parameter.Mode.HIDDEN) String currentPassword,
                                      @Param(mode = Parameter.Mode.HIDDEN) String newPassword) {
        Response response = given()
                .baseUri(props.baseUrl())
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(Map.of("currentPassword", currentPassword, "newPassword", newPassword))
                .when()
                .put("/api/users/me/password");

        attachResponseWithoutSecrets(response);
        return response;
    }

}
