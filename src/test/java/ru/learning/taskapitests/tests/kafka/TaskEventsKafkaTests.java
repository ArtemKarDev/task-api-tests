package ru.learning.taskapitests.tests.kafka;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.config.TestConfig;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.kafka.TaskEventMessage;
import ru.learning.taskapitests.utils.JsonUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(Tags.KAFKA)
@Tag(Tags.REGRESSION)
@Epic("Kafka")
@Feature("События task_out")
class TaskEventsKafkaTests extends TestBase {

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Создание задачи через REST порождает событие TASK_CREATED")
    void shouldPublishCreatedEventWhenTaskCreatedViaRest() {
        // Слушатель начинает читать ДО действия: так событие точно окажется после «конца» топика
        var listener = kafkaStep.startListening(TestConfig.getKafkaTopicOut());
        var request = TaskDataFactory.defaultTaskRequestDTO();

        long taskId = createTask(request);

        TaskEventMessage event = kafkaStep.awaitTaskEvent(listener, taskId, "TASK_CREATED");

        kafkaStep.checkTaskEvent(event, "TASK_CREATED", taskId, request, null);
    }

    @Test
    @DisplayName("PUT порождает TASK_UPDATED с новым состоянием, необязательное поле очищается")
    void shouldPublishUpdatedEventOnPut() {
        long taskId = createTask(TaskDataFactory.defaultTaskRequestDTO());
        var listener = kafkaStep.startListening(TestConfig.getKafkaTopicOut());
        var updated = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(TaskDataFactory.uniqueTitle("Updated"));
            r.status(CreateTaskRequest.StatusEnum.IN_PROGRESS);
            r.priority(CreateTaskRequest.PriorityEnum.HIGH);
            r.assignee("senior-tester");
            r.description(null);
        });

        Response response = step.updateTask(taskId, JsonUtils.toJson(updated));
        step.checkStatus(response, 200);

        TaskEventMessage event = kafkaStep.awaitTaskEvent(listener, taskId, "TASK_UPDATED");

        kafkaStep.checkTaskEvent(event, "TASK_UPDATED", taskId, updated, null);
    }

    @Test
    @DisplayName("PATCH порождает TASK_UPDATED, неизменённые поля остаются прежними")
    void shouldPublishUpdatedEventOnPatch() {
        var expected = TaskDataFactory.defaultTaskRequestDTO();
        long taskId = createTask(expected);
        var listener = kafkaStep.startListening(TestConfig.getKafkaTopicOut());

        Response response = step.patchTask(taskId, JsonUtils.toJson(Map.of("status", "DONE")));
        step.checkStatus(response, 200);

        expected.status(CreateTaskRequest.StatusEnum.DONE);
        TaskEventMessage event = kafkaStep.awaitTaskEvent(listener, taskId, "TASK_UPDATED");

        kafkaStep.checkTaskEvent(event, "TASK_UPDATED", taskId, expected, null);
    }

    @Test
    @DisplayName("Удаление задачи порождает TASK_DELETED с её последним состоянием")
    void shouldPublishDeletedEventWithLastSnapshot() {
        var request = TaskDataFactory.defaultTaskRequestDTO();
        long taskId = createTask(request);
        var listener = kafkaStep.startListening(TestConfig.getKafkaTopicOut());

        Response response = step.deleteTask(taskId);
        step.checkStatus(response, 204);

        TaskEventMessage event = kafkaStep.awaitTaskEvent(listener, taskId, "TASK_DELETED");

        kafkaStep.checkTaskEvent(event, "TASK_DELETED", taskId, request, null);
    }

    @Test
    @DisplayName("События одной задачи приходят в порядке действий: создание, изменение, удаление")
    void shouldKeepEventOrderForOneTask() {
        var listener = kafkaStep.startListening(TestConfig.getKafkaTopicOut());
        long taskId = createTask(TaskDataFactory.defaultTaskRequestDTO());
        step.checkStatus(step.patchTask(taskId, JsonUtils.toJson(Map.of("priority", "HIGH"))), 200);
        step.checkStatus(step.deleteTask(taskId), 204);

        // Дождались последнего события: все предыдущие этой задачи (одна партиция) уже получены
        kafkaStep.awaitTaskEvent(listener, taskId, "TASK_DELETED");

        kafkaStep.checkEventOrder(listener, taskId, "TASK_CREATED", "TASK_UPDATED", "TASK_DELETED");
    }

    @Test
    @Tag(Tags.NEGATIVE)
    @DisplayName("Отклонённый запрос не порождает события")
    void shouldNotPublishEventWhenRequestRejected() {
        var listener = kafkaStep.startListening(TestConfig.getKafkaTopicOut());
        String title = TaskDataFactory.uniqueTitle("NoEvent");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("priority", "LOW");          // статус не указан: запрос будет отклонён

        Response response = step.createTask(JsonUtils.toJson(body));
        step.checkStatus(response, 400);

        kafkaStep.checkNoMessagesContaining(listener, title, Duration.ofSeconds(3));
    }

    /** Создаёт задачу через REST (регистрирует её на очистку) и возвращает ID. */
    private long createTask(CreateTaskRequest request) {
        Response response = step.createTaskDto(request);
        step.checkStatus(response, 201);
        return response.jsonPath().getLong("id");
    }
}