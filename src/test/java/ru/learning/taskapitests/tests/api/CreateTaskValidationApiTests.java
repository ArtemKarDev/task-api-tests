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
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.utils.JsonUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Tag(Tags.NEGATIVE)
@Epic("API")
@Feature("Валидация создания задачи")
class CreateTaskValidationApiTests extends TestBase {

    private static final String PATH = "/api/tasks";
    private static final String UNREADABLE_BODY_MESSAGE =
            "Request body is missing, malformed or contains an invalid value";

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFields")
    @DisplayName("Некорректные поля отклоняются с описанием ошибки по полю")
    void shouldRejectInvalidFields(String caseName, Map<String, Object> body, List<String> expectedDetails) {
        Response response = step.createTask(JsonUtils.toJson(body));

        step.checkValidationError(response, PATH, expectedDetails.toArray(String[]::new));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unreadableBodies")
    @DisplayName("Нечитаемое тело запроса отклоняется общим сообщением")
    void shouldRejectUnreadableBody(String caseName, String body) {
        Response response = step.createTask(body);

        step.checkApiError(response, 400, UNREADABLE_BODY_MESSAGE, PATH);
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("title отсутствует", without("title"),
                        List.of("title: must not be blank")),
                Arguments.of("title равен null", with("title", null),
                        List.of("title: must not be blank")),
                Arguments.of("title пустая строка", with("title", ""),
                        List.of("title: must not be blank")),
                Arguments.of("title из пробелов", with("title", "   "),
                        List.of("title: must not be blank")),
                Arguments.of("title 256 символов", with("title", TaskDataFactory.uniqueTitleOfLength(256)),
                        List.of("title: must not exceed 255 characters")),
                Arguments.of("description 5001 символ", with("description", TaskDataFactory.stringOfLength(5001)),
                        List.of("description: must not exceed 5000 characters")),
                Arguments.of("assignee 101 символ", with("assignee", TaskDataFactory.stringOfLength(101)),
                        List.of("assignee: must not exceed 100 characters")),
                Arguments.of("status отсутствует", without("status"),
                        List.of("status: must not be null")),
                Arguments.of("priority отсутствует", without("priority"),
                        List.of("priority: must not be null")),
                Arguments.of("пустой объект", new LinkedHashMap<String, Object>(),
                        List.of("title: must not be blank",
                                "status: must not be null",
                                "priority: must not be null"))
        );
    }

    static Stream<Arguments> unreadableBodies() {
        return Stream.of(
                Arguments.of("обрыв JSON", "{\"title\":"),
                Arguments.of("не JSON", "это не json"),
                Arguments.of("пустое тело", ""),
                Arguments.of("лишнее поле", JsonUtils.toJson(with("unknown", "x"))),
                Arguments.of("title массивом", JsonUtils.toJson(with("title", List.of("a")))),
                Arguments.of("priority вне списка", JsonUtils.toJson(with("priority", "NOT_HIGH"))),
                Arguments.of("status вне списка", JsonUtils.toJson(with("status", "FINISHED"))),
                Arguments.of("status в нижнем регистре", JsonUtils.toJson(with("status", "new")))
        );
    }

    /** Корректное тело запроса. Тесты портят в нём ровно одно поле. */
    private static Map<String, Object> validBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", TaskDataFactory.uniqueTitle("Валидация"));
        body.put("description", "Описание");
        body.put("status", "NEW");
        body.put("priority", "MEDIUM");
        body.put("assignee", "tester");
        return body;
    }

    private static Map<String, Object> with(String field, Object value) {
        Map<String, Object> body = validBody();
        body.put(field, value);
        return body;
    }

    private static Map<String, Object> without(String field) {
        Map<String, Object> body = validBody();
        body.remove(field);
        return body;
    }
}