package ru.learning.taskapitests.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import ru.learning.taskapitests.config.TestConfig;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.kafka.KafkaProducerClient;
import ru.learning.taskapitests.kafka.TaskEventMessage;
import ru.learning.taskapitests.kafka.TopicListener;
import ru.learning.taskapitests.utils.JsonUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Шаги работы с Kafka. Соединения создаются по требованию и закрываются методом close() после теста. */
public class KafkaSteps implements AutoCloseable {

    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(20);
    // Строгий разбор: неизвестное поле в событии вызывает ошибку (так проверяется контракт события)
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<AutoCloseable> resources = new ArrayList<>();
    private KafkaProducerClient producer;

    @Step("Начать читать топик {topic} с текущего конца")
    public TopicListener startListening(String topic) {
        TopicListener listener = new TopicListener(TestConfig.getKafkaBootstrapServers(), topic);
        resources.add(listener);
        return listener;
    }

    @Step("Отправить сообщение в топик {topic}")
    public void send(String topic, String key, String value) {
        Allure.addAttachment("Сообщение (ключ: " + key + ")", "application/json", String.valueOf(value));
        producer().send(topic, key, value);
    }

    @Step("Отправить команду создания задачи в task_in, messageId = {messageId}")
    public void sendCreateCommand(String messageId, CreateTaskRequest request) {
        send(TestConfig.getKafkaTopicIn(), null, JsonUtils.toJson(commandBody(messageId, request)));
    }

    @Step("Дождаться события {eventType} для задачи {taskId}")
    public TaskEventMessage awaitTaskEvent(TopicListener listener, long taskId, String eventType) {
        TopicListener.Message message = listener.awaitMessage(
                candidate -> isEvent(candidate, taskId, eventType), EVENT_TIMEOUT);
        Allure.addAttachment("Событие " + eventType, "application/json", message.value());
        try {
            return MAPPER.readValue(message.value(), TaskEventMessage.class);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Событие не соответствует контракту: " + e.getOriginalMessage()
                    + "\n" + message.value(), e);
        }
    }

    @Step("Проверить событие {expectedType} для задачи {taskId}")
    public void checkTaskEvent(TaskEventMessage event, String expectedType, long taskId,
                               CreateTaskRequest expected, String expectedCorrelationId) {
        assertThat(event.eventType()).as("eventType").isEqualTo(expectedType);
        assertThat(UUID.fromString(event.eventId())).as("eventId (UUID)").isNotNull();
        assertThat(Instant.parse(event.occurredAt()))
                .as("occurredAt (время события, UTC)")
                .isBetween(Instant.now().minus(Duration.ofMinutes(10)), Instant.now().plus(Duration.ofMinutes(10)));
        assertThat(event.correlationId()).as("correlationId").isEqualTo(expectedCorrelationId);
        assertThat(event.taskId()).as("taskId").isEqualTo(taskId);

        TaskEventMessage.Snapshot task = event.task();
        assertThat(task).as("Снимок задачи в событии").isNotNull();
        assertThat(task.id()).as("task.id").isEqualTo(taskId);
        assertThat(task.title()).as("task.title").isEqualTo(expected.getTitle());
        assertThat(task.description()).as("task.description").isEqualTo(expected.getDescription());
        assertThat(task.status()).as("task.status").isEqualTo(expected.getStatus().toString());
        assertThat(task.priority()).as("task.priority").isEqualTo(expected.getPriority().toString());
        assertThat(task.assignee()).as("task.assignee").isEqualTo(expected.getAssignee());
        // Формат времени: разбор должен пройти без ошибки
        assertThat(LocalDateTime.parse(task.createdAt())).as("task.createdAt").isNotNull();
        assertThat(LocalDateTime.parse(task.updatedAt())).as("task.updatedAt").isNotNull();
    }

    @Override
    public void close() {
        for (AutoCloseable resource : resources) {
            try {
                resource.close();
            } catch (Exception ignored) {
                // закрытие соединений не должно скрывать результат теста
            }
        }
        resources.clear();
        if (producer != null) {
            producer.close();
            producer = null;
        }
    }

    private KafkaProducerClient producer() {
        if (producer == null) {
            producer = new KafkaProducerClient(TestConfig.getKafkaBootstrapServers());
        }
        return producer;
    }

    private Map<String, Object> commandBody(String messageId, CreateTaskRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messageId", messageId);
        body.put("title", request.getTitle());
        body.put("description", request.getDescription());
        body.put("status", request.getStatus() == null ? null : request.getStatus().toString());
        body.put("priority", request.getPriority() == null ? null : request.getPriority().toString());
        body.put("assignee", request.getAssignee());
        return body;
    }

    private boolean isEvent(TopicListener.Message message, long taskId, String eventType) {
        if (!String.valueOf(taskId).equals(message.key())) {
            return false;
        }
        try {
            return eventType.equals(MAPPER.readTree(message.value()).path("eventType").asText());
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}