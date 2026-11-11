package ru.learning.taskapitests.client;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.ui.pages.LoginPage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

@Component
public class UiSessionClient {

    private static final Pattern CSRF = Pattern.compile("<input[^>]*name=\"_csrf\"[^>]*value=\"([^\"]+)\"");

    private final TestProperties props;

    public UiSessionClient(TestProperties props) {
        this.props = props;
    }

    /** Входит через форму по HTTP и возвращает cookie авторизованной сессии. */
    public Map<String, String> login(Role role) {
        var account = props.auth().accountFor(role);

        Response loginPage = given()
                .baseUri(props.baseUrl())
                .get(LoginPage.PATH);
        String csrf = extractCsrf(loginPage.asString());

        Response result = given()
                .baseUri(props.baseUrl())
                .cookies(loginPage.cookies())
                .contentType(ContentType.URLENC)
                .formParam("username", account.username())
                .formParam("password", account.password())
                .formParam("_csrf", csrf)
                .redirects().follow(false)
                .post(LoginPage.PATH);

        String location = result.header("Location");
        if (result.statusCode() != 302 || location == null || location.contains("error")) {
            throw new IllegalStateException("Не удалось войти по HTTP ролью " + role
                    + ": код " + result.statusCode() + ", Location " + location);
        }

        Map<String, String> cookies = new HashMap<>(loginPage.cookies());
        cookies.putAll(result.cookies());
        return cookies;
    }

    private String extractCsrf(String html) {
        Matcher matcher = CSRF.matcher(html);
        if (!matcher.find()) {
            throw new IllegalStateException("На странице входа не найден токен _csrf");
        }
        return matcher.group(1);
    }
}