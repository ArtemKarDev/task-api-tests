package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.generated.model.TokenResponse;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.learning.taskapitests.client.AuthApiClient.LOGIN_PATH;

@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Epic("Авторизация")
@Feature("Вход")
@DisplayName("Авторизация с получением токена")
public class AuthApiTests extends TestBase {

    private enum Variant {
        VALID, WRONG
    }

    @Test
    @DisplayName("Успешный вход с верным логином и паролем")
    public void shouldReturnTokenOnValidLogin() {
        Response response = authApi.login(
                props.auth().user().username(),
                props.auth().user().password()
        );
        step.checkStatus(response, 200);
        TokenResponse tokenResponse = response.as(TokenResponse.class);
        assertThat(tokenResponse.getAccessToken()).as("В ответе присутствует токен").isNotBlank();
        assertThat(tokenResponse.getTokenType()).as("Тип токена - Bearer").isEqualTo("Bearer");
        assertThat(tokenResponse.getExpiresIn()).as("Срок жизни токена от 1 до 900 секунд").isBetween(1L, 900L);

    }

    @ParameterizedTest(name = "[{index}] логин: {0}, пароль: {1}")
    @CsvSource({
            "VALID, WRONG",
            "WRONG, VALID"
    })
    @Tag(Tags.NEGATIVE)
    @DisplayName("Ошибка входа с неверным логином или паролем")
    public void shouldReturn401ForWrongUsernameOrPassword(Variant login, Variant password) {
        String username = login == Variant.VALID ? props.auth().user().username() : "wrong_username";
        String pass = password == Variant.VALID ? props.auth().user().password() : "wrong_password";

        Response response = authApi.login(username, pass);
        step.checkApiError(response, 401,
                "Invalid username or password",
                LOGIN_PATH);
    }


    @ParameterizedTest(name = "[{index}] {0} ")
    @MethodSource("blankFieldsCases")
    @Tag(Tags.NEGATIVE)
    @DisplayName("Пустые поля: 400 Validation failed с описанием")
    public void shouldReturn400ForBlankFields(String caseName, String username, String password, List<String> details) {
        Response response = authApi.login(username, password);
        step.checkApiErrorWithDetails(
                response,
                400,
                "Validation failed",
                LOGIN_PATH,
                details.toArray(new String[0]));
    }


    @ParameterizedTest(name = "[{index}] {0} ")
    @MethodSource("malformedBodyCases")
    @Tag(Tags.NEGATIVE)
    @DisplayName("Невалидное тело запроса: 400")
    public void shouldReturn400ForBrokenBody(String caseName, String body) {
        Response response = authApi.loginRaw(body);
        step.checkApiError(response, 400, "Request body is missing, malformed or contains an invalid value", LOGIN_PATH);
    }

    static Stream<Arguments> blankFieldsCases() {
        return Stream.of(
                Arguments.of("поля не заданы", null, null,
                        List.of("password: must not be blank", "username: must not be blank")),
                Arguments.of("пустой логин", "", "x",
                        List.of("username: must not be blank")),
                Arguments.of("пустой пароль", "user", "",
                        List.of("password: must not be blank"))
        );
    }

    static Stream<Arguments> malformedBodyCases() {
        String extraField = """
                {"username":"user","password":"x","extra":1}
                """;

        return Stream.of(
                Arguments.of("нет тела", null),
                Arguments.of("лишнее поле", extraField),
                Arguments.of("битый JSON", "{это не json")
        );
    }

}
