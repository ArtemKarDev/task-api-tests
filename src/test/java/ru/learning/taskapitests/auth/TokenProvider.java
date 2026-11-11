package ru.learning.taskapitests.auth;

import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.client.AuthApiClient;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.generated.model.LoginRequest;

import java.util.EnumMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Component
public class TokenProvider {

    private final TestProperties props;

    // Токены хранятся по потокам: параллельные тесты не делят словарь и не стирают токены друг друга
    private final ThreadLocal<Map<Role, String>> tokens = ThreadLocal.withInitial(() -> new EnumMap<>(Role.class));

    public TokenProvider(TestProperties props) {
        this.props = props;
    }

    public String login(Role role) {

        TestProperties.Account account = account(role);

        LoginRequest login = new LoginRequest();
        login.setUsername(account.username());
        login.setPassword(account.password());

        return given()
                .baseUri(props.baseUrl())
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post(AuthApiClient.LOGIN_PATH)
                .then().statusCode(200).extract().path("accessToken");
    }

    private TestProperties.Account account(Role role) {
        return switch (role) {
            case USER -> props.auth().user();
            case ADMIN -> props.auth().admin();
        };
    }

    public String tokenFor(Role role) {
        return tokens.get().computeIfAbsent(role, this::login);
    }

    public void reset() {
        tokens.remove();
    }

}
