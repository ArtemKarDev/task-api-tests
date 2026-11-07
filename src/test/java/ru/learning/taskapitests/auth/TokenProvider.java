package ru.learning.taskapitests.auth;

import io.restassured.http.ContentType;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.generated.model.LoginRequest;

import java.util.EnumMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Component
public class TokenProvider {


    private final TestProperties props;

    //    Не потокобезопасно: годится, пока тесты идут по очереди;
    //    при параллельном запуске заменить на хранение по потокам
    private final Map<Role, String> tokens = new EnumMap<>(Role.class);

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
                .post("/api/auth/login")
                .then().statusCode(200).extract().path("accessToken");
    }

    private TestProperties.Account account(Role role) {
        return switch (role) {
            case USER -> props.auth().user();
            case ADMIN -> props.auth().admin();
        };
    }


    public String tokenFor(Role role) {
        return tokens.computeIfAbsent(role, this::login);
    }

    public void reset() {
        tokens.clear();
    }

}
