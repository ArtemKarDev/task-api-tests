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
import ru.learning.taskapitests.kafka.TaskEventMessage;

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

        Response response = step.createTaskDto(request);
        step.checkStatus(response, 201);
        long taskId = response.jsonPath().getLong("id");

        TaskEventMessage event = kafkaStep.awaitTaskEvent(listener, taskId, "TASK_CREATED");

        kafkaStep.checkTaskEvent(event, "TASK_CREATED", taskId, request, null);
    }
}