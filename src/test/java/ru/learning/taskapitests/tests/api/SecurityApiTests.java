package ru.learning.taskapitests.tests.api;


import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.auth.Role;

import java.util.stream.Stream;

@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Tag(Tags.NEGATIVE)
@Epic("Авторизация")
@Feature("Защита API")
@DisplayName("Проверки работы защиты при невалидных запросах")
public class SecurityApiTests extends TestBase {

    private static final String MISSING_ID = "999999999";
    private static final String EMPTY_JSON = "{}";
    private static final String ERROR_AUTH = "Authentication required";
    private static final String ERROR_TOKEN = "Invalid or expired token";
    private static final String ERROR_ROLE = "Access denied: insufficient permissions";
    private static final String JSON = "application/json";

    @ParameterizedTest(name = "[{index}] метод: {0}, путь: {1}")
    @MethodSource("protectedEndpoints")
    @DisplayName("Запросы без авторизации возвращают 401")
    public void shouldReturn401ForRequestWithoutToken(String method, String path, String body) {
        Response response = step.sendRequestAnonymous(method, path, JSON, body);
        step.checkApiError(response, 401, ERROR_AUTH, path);
    }


    @ParameterizedTest(name = "[{index}] метод: {0}, путь: {1}")
    @MethodSource("protectedEndpoints")
    @DisplayName("Запросы с невалидным токеном возвращают 401")
    public void shouldReturn401ForRequestWithBadToken(String method, String path, String body) {
        Response response = step.sendRequestWithToken("not-a-valid-token", method, path, JSON, body);
        step.checkApiError(response, 401, ERROR_TOKEN, path);
    }

    @ParameterizedTest(name = "[{index}] метод: {0}, путь: {1}")
    @MethodSource("adminOnlyEndpoints")
    @DisplayName("Запросы от имени user возвращают 403")
    public void shouldReturn403ForUserRoleRequest(String method, String path, String body) {
        Response response = step.sendRequest(Role.USER, method, path, JSON, body);
        step.checkApiError(response, 403, ERROR_ROLE, path);
    }

    static Stream<Arguments> commonEndpoints() {
        return Stream.of(
                Arguments.of("GET", "/api/tasks", null),
                Arguments.of("GET", "/api/tasks/" + MISSING_ID, null),
                Arguments.of("POST", "/api/tasks", EMPTY_JSON),
                Arguments.of("PUT", "/api/tasks/" + MISSING_ID, EMPTY_JSON),
                Arguments.of("PATCH", "/api/tasks/" + MISSING_ID, EMPTY_JSON),
                Arguments.of("PUT", "/api/users/me/password", EMPTY_JSON)
        );
    }

    static Stream<Arguments> adminOnlyEndpoints() {
        return Stream.of(
                Arguments.of("DELETE", "/api/tasks/" + MISSING_ID, null),
                Arguments.of("GET", "/api/admin/users", null),
                Arguments.of("POST", "/api/admin/users", EMPTY_JSON),
                Arguments.of("PATCH", "/api/admin/users/" + MISSING_ID, EMPTY_JSON),
                Arguments.of("PUT", "/api/admin/users/" + MISSING_ID + "/password", EMPTY_JSON),
                Arguments.of("DELETE", "/api/admin/users/" + MISSING_ID, null)
        );
    }

    static Stream<Arguments> protectedEndpoints() {
        return Stream.concat(commonEndpoints(), adminOnlyEndpoints());
    }
}
