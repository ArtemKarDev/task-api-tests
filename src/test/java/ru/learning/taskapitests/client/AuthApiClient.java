package ru.learning.taskapitests.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.generated.model.LoginRequest;
import ru.learning.taskapitests.utils.AllureAttachments;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;


@Component
public class AuthApiClient {

    private final TestProperties props;

    public AuthApiClient(TestProperties props) {
        this.props = props;
    }

    @Step("Войти как {username}")
    public Response login(String username, String password) {

        LoginRequest request = new LoginRequest().username(username).password(password);

        Response response = given()
                .baseUri(props.baseUrl())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login");
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
