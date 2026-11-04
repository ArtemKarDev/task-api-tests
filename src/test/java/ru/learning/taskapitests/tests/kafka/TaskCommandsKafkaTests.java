package ru.learning.taskapitests.tests.kafka;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.kafka.DeadLetterMessage;
import ru.learning.taskapitests.kafka.TaskEventMessage;
import ru.learning.taskapitests.utils.JsonUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Tag(Tags.KAFKA)
@Tag(Tags.REGRESSION)
@Epic("Kafka")
@Feature("Команды task_in")
class TaskCommandsKafkaTests extends TestBase {

    private static final String UNREADABLE =
            "Message is not valid JSON or contains unknown fields or invalid values";
    private static final String BAD_MESSAGE_ID = "messageId is missing or is not a UUID";
    private static final String VALIDATION = "Validation failed: ";

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Команда в task_in создаёт задачу, событие несёт messageId в correlationId")
    void shouldCreateTaskFromKafkaCommand() {
        var out = kafkaStep.startListening(props.kafka().topicOut());
        String messageId = UUID.randomUUID().toString();
        var request = TaskDataFactory.defaultTaskRequestDTO();

        kafkaStep.sendCreateCommand(messageId, request);

        TaskEventMessage event = kafkaStep.awaitCreatedEventByCorrelation(out, messageId);
        long taskId = event.taskId();
        step.registerTaskForCleanup(taskId);

        kafkaStep.checkTaskEvent(event, "TASK_CREATED", taskId, request, messageId);
        Response response = step.getTask(taskId);
        step.checkStatus(response, 200);
        step.checkResponseCreateTaskDataWithDto(response, request);
        dbStep.checkTaskSaved(taskId, request);
        dbStep.checkMessageProcessed(UUID.fromString(messageId), taskId);
    }

    @ParameterizedTest(name = "исполнитель: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "     "})
    @DisplayName("Пустой или пробельный исполнитель в команде сохраняется как «не указан»")
    void shouldStoreBlankAssigneeAsNull(String assignee) {
        var out = kafkaStep.startListening(props.kafka().topicOut());
        String messageId = UUID.randomUUID().toString();
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.assignee(assignee));

        kafkaStep.sendCreateCommand(messageId, request);

        TaskEventMessage event = kafkaStep.awaitCreatedEventByCorrelation(out, messageId);
        long taskId = event.taskId();
        step.registerTaskForCleanup(taskId);

        // Команда уже отправлена; для проверки ожидаем «не указан» (null)
        request.assignee(null);
        kafkaStep.checkTaskEvent(event, "TASK_CREATED", taskId, request, messageId);
        dbStep.checkTaskSaved(taskId, request);
    }

    @Test
    @DisplayName("Повторная доставка команды с тем же messageId не создаёт вторую задачу и второе событие")
    void shouldIgnoreRepeatedCommand() {
        var out = kafkaStep.startListening(props.kafka().topicOut());
        String messageId = UUID.randomUUID().toString();
        var request = TaskDataFactory.defaultTaskRequestDTO();

        kafkaStep.sendCreateCommand(messageId, request);
        kafkaStep.sendCreateCommand(messageId, request);

        TaskEventMessage first = kafkaStep.awaitCreatedEventByCorrelation(out, messageId);
        step.registerTaskForCleanup(first.taskId());

        // Второе сообщение обрабатывается сразу после первого; ждём и убеждаемся, что дубля нет
        kafkaStep.checkCreatedEventsCount(out, messageId, Duration.ofSeconds(3), 1);
        dbStep.checkTasksCountByTitle(request.getTitle(), 1);
        dbStep.checkProcessedCount(UUID.fromString(messageId), 1);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCommands")
    @Tag(Tags.NEGATIVE)
    @DisplayName("Некорректная команда уходит в task_in_dlq с причиной и не создаёт задачу")
    void shouldMoveInvalidCommandToDeadLetterTopic(String caseName, String payload,
                                                   String expectedError, String title) {
        var dlq = kafkaStep.startListening(props.kafka().topicInDlq());

        kafkaStep.send(props.kafka().topicIn(), null, payload);

        DeadLetterMessage message = kafkaStep.awaitDeadLetter(dlq, payload);
        kafkaStep.checkDeadLetter(message, payload, expectedError);
        // Сообщение уже обработано (отклонено), поэтому задача, если бы создавалась, уже была бы в БД
        if (title != null) {
            dbStep.checkTasksCountByTitle(title, 0);
        }
    }

    static Stream<Arguments> invalidCommands() {
        return Stream.of(
                rawCase("не JSON", "not json " + UUID.randomUUID(), UNREADABLE),
                mapCase("лишнее поле", m -> m.put("extra", "x"), UNREADABLE),
                mapCase("статус вне списка", m -> m.put("status", "FINISHED"), UNREADABLE),
                mapCase("приоритет вне списка", m -> m.put("priority", "URGENT"), UNREADABLE),
                mapCase("messageId отсутствует", m -> m.remove("messageId"), BAD_MESSAGE_ID),
                mapCase("messageId равен null", m -> m.put("messageId", null), BAD_MESSAGE_ID),
                mapCase("messageId не UUID", m -> m.put("messageId", "bad-1"), BAD_MESSAGE_ID),
                mapCase("название пустое", m -> m.put("title", ""),
                        VALIDATION + "title: must not be blank"),
                mapCase("название из пробелов", m -> m.put("title", "   "),
                        VALIDATION + "title: must not be blank"),
                mapCase("название отсутствует", m -> m.remove("title"),
                        VALIDATION + "title: must not be blank"),
                mapCase("статус отсутствует", m -> m.remove("status"),
                        VALIDATION + "status: must not be null"),
                mapCase("название 256 символов",
                        m -> m.put("title", TaskDataFactory.uniqueTitleOfLength(256)),
                        VALIDATION + "title: must not exceed 255 characters"),
                mapCase("исполнитель 101 символ",
                        m -> m.put("assignee", TaskDataFactory.stringOfLength(101)),
                        VALIDATION + "assignee: must not exceed 100 characters"),
                mapCase("описание 5001 символ",
                        m -> m.put("description", TaskDataFactory.stringOfLength(5001)),
                        VALIDATION + "description: must not exceed 5000 characters"),
                mapCase("ошибки в нескольких полях", m -> {
                    m.put("title", "");
                    m.remove("status");
                    m.remove("priority");
                }, VALIDATION + "priority: must not be null; status: must not be null; title: must not be blank")
        );
    }

    /** Случай с заранее готовым текстом сообщения (не JSON). Названия задачи нет, проверка БД не нужна. */
    private static Arguments rawCase(String caseName, String payload, String expectedError) {
        return Arguments.of(caseName, payload, expectedError, null);
    }

    /** Случай на основе корректной команды, которую портит переданное действие. */
    private static Arguments mapCase(String caseName, Consumer<Map<String, Object>> spoil, String expectedError) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messageId", UUID.randomUUID().toString());
        body.put("title", TaskDataFactory.uniqueTitle("Dlq"));
        body.put("description", "Описание");
        body.put("status", "NEW");
        body.put("priority", "LOW");
        body.put("assignee", "tester");
        spoil.accept(body);

        Object title = body.get("title");
        String titleToCheck = title instanceof String text && !text.isBlank() ? text : null;
        return Arguments.of(caseName, JsonUtils.toJson(body), expectedError, titleToCheck);
    }
}