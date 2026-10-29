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

import java.util.List;
import java.util.stream.Stream;

@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Tag(Tags.NEGATIVE)
@Epic("API")
@Feature("Адрес и идентификатор")
class RoutingAndIdApiTests extends TestBase {

    private static final String JSON = "application/json";
    private static final String VALID_BODY = "{\"title\":\"x\",\"status\":\"NEW\",\"priority\":\"LOW\"}";
    private static final List<String> ID_METHODS = List.of("GET", "PUT", "PATCH", "DELETE");

    @ParameterizedTest(name = "{0} /api/tasks/{1}")
    @MethodSource("methodsAndInvalidIds")
    @DisplayName("ID не число или не помещается в long: 400 с описанием параметра")
    void shouldRejectInvalidId(String method, String id) {
        String path = "/api/tasks/" + id;

        Response response = step.sendRequest(method, path, JSON, bodyFor(method));

        step.checkApiErrorWithDetails(response, 400, "Invalid request parameter", path,
                "id: invalid value '" + id + "'");
    }

    @ParameterizedTest(name = "{0} /api/tasks/{1}")
    @MethodSource("methodsAndNonPositiveIds")
    @DisplayName("Нулевой и отрицательный ID: как у любой несуществующей задачи, 404")
    void shouldReturn404ForNonPositiveId(String method, String id) {
        String path = "/api/tasks/" + id;

        Response response = step.sendRequest(method, path, JSON, bodyFor(method));

        step.checkApiError(response, 404, "Task not found with id: " + id, path);
    }

    @ParameterizedTest(name = "GET {0}")
    @CsvSource({
            "/api/unknown",
            "/api/tasks/1/extra",
            "/api/unknown/deep/path"
    })
    @DisplayName("Неизвестный адрес API: 404 в едином формате")
    void shouldReturn404ForUnknownAddress(String path) {
        Response response = step.sendRequest("GET", path, JSON, null);

        step.checkApiError(response, 404, "Resource not found", path);
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("notAllowedMethods")
    @DisplayName("Метод не поддерживается адресом: 405 и заголовок Allow")
    void shouldReturn405ForWrongMethod(String method, String path, List<String> allowedMethods) {
        Response response = step.sendRequest(method, path, JSON, bodyFor(method));

        step.checkApiError(response, 405, "Method Not Allowed", path);
        step.checkHeaderContains(response, "Allow", allowedMethods);
    }

    @Test
    @DisplayName("Тело не в формате JSON: 415")
    void shouldReturn415ForNonJsonBody() {
        Response response = step.sendRequest("POST", "/api/tasks", "text/plain", "hello");

        step.checkApiError(response, 415, "Unsupported Media Type", "/api/tasks");
    }

    static Stream<Arguments> methodsAndInvalidIds() {
        return combine(ID_METHODS, List.of("abc", "99999999999999999999", "1.5"));
    }

    static Stream<Arguments> methodsAndNonPositiveIds() {
        return combine(ID_METHODS, List.of("0", "-5"));
    }

    static Stream<Arguments> notAllowedMethods() {
        List<String> collection = List.of("GET", "POST");
        return Stream.of(
                Arguments.of("DELETE", "/api/tasks", collection),
                Arguments.of("PUT", "/api/tasks", collection),
                Arguments.of("PATCH", "/api/tasks", collection),
                Arguments.of("POST", "/api/tasks/1", List.of("GET", "PUT", "PATCH", "DELETE"))
        );
    }

    /** Все сочетания «метод и значение ID». */
    private static Stream<Arguments> combine(List<String> methods, List<String> ids) {
        return methods.stream().flatMap(method -> ids.stream().map(id -> Arguments.of(method, id)));
    }

    /** Тело нужно только методам, которые его принимают. */
    private static String bodyFor(String method) {
        return List.of("PUT", "PATCH", "POST").contains(method) ? VALID_BODY : null;
    }
}