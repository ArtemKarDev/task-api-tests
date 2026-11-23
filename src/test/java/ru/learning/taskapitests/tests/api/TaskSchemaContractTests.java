package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learning.taskapitests.Tags;

import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.not;
import static ru.learning.taskapitests.steps.TaskSteps.TASK_SCHEMA;

@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Tag(Tags.NEGATIVE)
@Epic("Контракт API")
@Feature("Схема задачи")
@DisplayName("Схема задачи task.json")
class TaskSchemaContractTests{

//    private static final String TASK_SCHEMA = "schemas/task.json";
    private static final String VALID_TASK = """
            {"id":15,"title":"Купить молоко","description":null,"status":"NEW","priority":"LOW",
             "assignee":null,"createdAt":"2026-10-22T15:13:05.04423","updatedAt":"2026-10-22T15:13:05.04423"}
            """;


    @Test
    @DisplayName("Правильная задача соответствует схеме")
    void validTaskMatchesSchema() {
        MatcherAssert.assertThat(VALID_TASK, matchesJsonSchemaInClasspath(TASK_SCHEMA));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("brokenTasks")
    void brokenTaskDoesNotMatchSchema(String violation, String json) {
        MatcherAssert.assertThat(json, not(matchesJsonSchemaInClasspath(TASK_SCHEMA)));
    }

    static Stream<Arguments> brokenTasks() {
        return Stream.of(
                Arguments.of("нет обязательного поля id",
                        VALID_TASK.replace("\"id\":15,", "")),
                Arguments.of("id строкой вместо числа",
                        VALID_TASK.replace("\"id\":15", "\"id\":\"15\"")),
                Arguments.of("неизвестный статус",
                        VALID_TASK.replace("\"status\":\"NEW\"", "\"status\":\"URGENT\"")),
                Arguments.of("лишнее поле password",
                        VALID_TASK.replace("\"id\":15,", "\"id\":15,\"password\":\"secret\",")),
                Arguments.of("дата не по формату",
                        VALID_TASK.replace("\"createdAt\":\"2026-10-22T15:13:05.04423\"", "\"createdAt\":\"вчера\"")),
                Arguments.of("пустой заголовок",
                        VALID_TASK.replace("\"title\":\"Купить молоко\"", "\"title\":\"\""))
        );
    }

}
