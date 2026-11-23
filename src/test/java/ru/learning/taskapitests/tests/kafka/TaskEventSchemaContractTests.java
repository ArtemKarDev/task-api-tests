package ru.learning.taskapitests.tests.kafka;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.matchers.Matches;

import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.not;
import static ru.learning.taskapitests.steps.KafkaSteps.EVENT_SCHEMA;

public class TaskEventSchemaContractTests {

    private static final String VALID_EVENT = """
            {"eventId":"702358d2-9dc5-4a8e-972f-dcb11d630da3","eventType":"TASK_CREATED",
             "occurredAt":"2026-11-15T17:44:58.282387600Z","correlationId":null,"taskId":996,
             "task":{"id":996,"title":"Задача","description":null,"status":"NEW","priority":"MEDIUM",
                     "assignee":"qa","createdAt":"2026-11-15T20:44:58.2327274","updatedAt":"2026-11-15T20:44:58.2327274"}}
            """;

    static Stream<Arguments> brokenEvents() {
        return Stream.of(
                Arguments.of("неизвестный eventType",
                        VALID_EVENT.replace("\"eventType\":\"TASK_CREATED\"", "\"eventType\":\"TASK_ARCHIVED\"")),
                Arguments.of("eventId не UUID",
                        VALID_EVENT.replace("\"eventId\":\"702358d2-9dc5-4a8e-972f-dcb11d630da3\"", "\"eventId\":\"not-a-uuid\"")),
                Arguments.of("occurredAt без часового пояса",
                        VALID_EVENT.replace("\"occurredAt\":\"2026-11-15T17:44:58.282387600Z\"", "\"occurredAt\":\"2026-11-15T17:44:58.282387600\"")),
                Arguments.of("taskId строкой вместо числа",
                        VALID_EVENT.replace("\"taskId\":996", "\"taskId\":\"996\"")),
                Arguments.of("лишнее поле верхнего уровня",
                        VALID_EVENT.replace("\"taskId\":996,", "\"taskId\":996,\"version\":2,")),
                Arguments.of("нет снимка задачи task",
                        VALID_EVENT.replaceAll("(?s),\\s*\"task\":\\{.*}\\s*}", "}")),
                Arguments.of("неизвестный статус внутри task",
                        VALID_EVENT.replace("\"status\":\"NEW\"", "\"status\":\"URGENT\"")),
                Arguments.of("лишнее поле внутри task",
                        VALID_EVENT.replace("\"id\":996,", "\"id\":996,\"secret\":\"x\",")),
                Arguments.of("дата задачи с часовым поясом",
                        VALID_EVENT.replace("\"createdAt\":\"2026-11-15T20:44:58.2327274\"", "\"createdAt\":\"2026-11-15T20:44:58.2327274Z\""))
        );

    }

    @Test
    @DisplayName("")
    void shouldEventMatchesSchema() {
        MatcherAssert.assertThat(VALID_EVENT, matchesJsonSchemaInClasspath(EVENT_SCHEMA));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("brokenEvents")
    @DisplayName("Событие с нарушением контракта не соответствует схеме")
    void brokenEventDoesNotMatchSchema(String violation, String json) {
        MatcherAssert.assertThat(json, not(matchesJsonSchemaInClasspath(EVENT_SCHEMA)));
    }

}
