package ru.learning.taskapitests.tests.api;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.not;
import static ru.learning.taskapitests.steps.TaskSteps.ERROR_SCHEMA;

public class ErrorSchemaContractTests {

    private static final String VALID_ERROR = """
        {"timestamp":"2026-11-23T09:55:00.123Z","status":400,"error":"Bad Request",
         "message":"Validation failed","details":[{"field":"title","message":"must not be blank"}],
         "path":"/api/tasks"}
        """;

    @Test
    @DisplayName("Ошибка соответствует схеме")
    void validErrorMatchesSchema() {
        MatcherAssert.assertThat(VALID_ERROR, matchesJsonSchemaInClasspath(ERROR_SCHEMA));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("brokenErrors")
    @DisplayName("Ошибка с нарушением контракта не соответствует схеме")
    void brokenErrorDoesNotMatchSchema(String violation, String json) {
        MatcherAssert.assertThat(json, not(matchesJsonSchemaInClasspath(ERROR_SCHEMA)));
    }

    static Stream<Arguments> brokenErrors() {
        return Stream.of(
                Arguments.of("нет обязательного поля message",
                        VALID_ERROR.replace("\"message\":\"Validation failed\",", "")),
                Arguments.of("статус 200 в теле ошибки",
                        VALID_ERROR.replace("\"status\":400", "\"status\":200")),
                Arguments.of("статус строкой вместо числа",
                        VALID_ERROR.replace("\"status\":400", "\"status\":\"400\"")),
                Arguments.of("timestamp без часового пояса",
                        VALID_ERROR.replace("\"2026-11-23T09:55:00.123Z\"", "\"2026-11-23T09:55:00.123\"")),
                Arguments.of("details строкой вместо массива",
                        VALID_ERROR.replace("[{\"field\":\"title\",\"message\":\"must not be blank\"}]", "\"title is blank\"")),
                Arguments.of("элемент details без field",
                        VALID_ERROR.replace("{\"field\":\"title\",", "{")),
                Arguments.of("лишнее поле в элементе details",
                        VALID_ERROR.replace("\"message\":\"must not be blank\"}", "\"message\":\"must not be blank\",\"code\":\"NotBlank\"}")),
                Arguments.of("лишнее поле stackTrace",
                        VALID_ERROR.replace("\"path\":\"/api/tasks\"", "\"path\":\"/api/tasks\",\"stackTrace\":\"at ru.learning...\"")),
                Arguments.of("path не начинается с /",
                        VALID_ERROR.replace("\"path\":\"/api/tasks\"", "\"path\":\"api/tasks\""))
        );
    }


}
