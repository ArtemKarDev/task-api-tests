package ru.learning.taskapitests.steps;

import io.qameta.allure.Step;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.db.TaskDbRecord;
import ru.learning.taskapitests.db.TaskRepository;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Component
public class DbSteps {

    private static final Duration DB_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DB_POLL = Duration.ofMillis(200);

    private final TaskRepository taskRepository;

    public DbSteps(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    @Step("Получить задачу {taskId} из БД")
    public TaskDbRecord getTaskFromDb(long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() ->  new AssertionError("Задачи " + taskId + " нет в БД"));
    }

    @Step("Проверить, что задача {taskId} сохранена в БД без искажений")
    public void checkTaskSaved(long taskId, CreateTaskRequest expected){
        TaskDbRecord record =  getTaskFromDb(taskId);
        assertThat(record).as("Запись в БД").isNotNull();
        assertThat(record.title()).as("Название в БД").isEqualTo(expected.getTitle());
        assertThat(record.description()).as("Описание в БД").isEqualTo(expected.getDescription());
        assertThat(record.assignee()).as("Исполнитель в БД").isEqualTo(expected.getAssignee());
        assertThat(record.priority()).as("Приоритет в БД").isEqualTo(expected.getPriority().getValue());
        assertThat(record.status()).as("Статус в БД").isEqualTo(expected.getStatus().getValue());
        assertThat(record.createdAt()).as("Время создания в БД").isNotNull();
        assertThat(record.updatedAt()).as("Время изменения в БД").isNotNull();
    }

    @Step("Проверить необязательные поля задачи {taskId} в БД")
    public void checkOptionalFields(long taskId, String expectedDescription, String expectedAssignee){
        TaskDbRecord dbTask = getTaskFromDb(taskId);
        assertThat(dbTask).as("Запись в БД").isNotNull();
        assertThat(dbTask.description()).as("Описание в БД").isEqualTo(expectedDescription);
        assertThat(dbTask.assignee()).as("Исполнитель в БД").isEqualTo(expectedAssignee);
        
    }

    @Step("Проверить, что в БД задач с названием «{title}»: {expected}")
    public void checkTasksCountByTitle(String title, long expected) {
        assertThat(taskRepository.countByTitle(title))
                .as("Количество задач с названием «%s» в БД", title).isEqualTo(expected);
    }

    @Step("Проверить, что сообщение {messageId} учтено как обработанное и привело к задаче {taskId}")
    public void checkMessageProcessed(UUID messageId, long taskId) {
        assertThat(taskRepository.findTaskIdByMessage(messageId))
                .as("Задача, привязанная к сообщению %s в processed_messages", messageId)
                .contains(taskId);
    }

    @Step("Проверить, что записей об обработке сообщения {messageId}: {expected}")
    public void checkProcessedCount(UUID messageId, long expected) {
        assertThat(taskRepository.countProcessedMessages(messageId))
                .as("Записей в processed_messages для сообщения %s", messageId)
                .isEqualTo(expected);
    }


    // проверки с ожиданиями по БД пока не используются, сейчас это работает по сигналам в кафка

    @Step("Дождаться, что сообщение {messageId} обработано и привело к задаче {taskId}")
    public void awaitMessageProcessed(UUID messageId, long taskId) {
        await("обработка сообщения " + messageId)
                .atMost(DB_TIMEOUT)
                .pollInterval(DB_POLL)
                .untilAsserted(() -> assertThat(taskRepository.findTaskIdByMessage(messageId))
                        .as("Задача, привязанная к сообщению %s", messageId)
                        .contains(taskId));
    }

    @Step("Проверить, что записей об обработке сообщения {messageId} остаётся {expected} в течение {stable}")
    public void checkProcessedCountStays(UUID messageId, long expected, Duration stable) {
        await("число записей для сообщения " + messageId + " не меняется")
                .during(stable)
                .atMost(stable.plus(DB_TIMEOUT))
                .pollInterval(DB_POLL)
                .untilAsserted(() -> assertThat(taskRepository.countProcessedMessages(messageId))
                        .as("Записей в processed_messages для сообщения %s", messageId)
                        .isEqualTo(expected));
    }

    @Step("Удалить задачу {taskId} напрямую в БД")
    public void deleteTaskInDb(long taskId) {
        assertThat(taskRepository.deleteById(taskId))
                .as("Удалено строк в БД")
                .isEqualTo(1);
    }

}
