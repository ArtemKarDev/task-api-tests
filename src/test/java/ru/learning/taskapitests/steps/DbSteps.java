package ru.learning.taskapitests.steps;

import io.qameta.allure.Step;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.db.TaskDbRecord;
import ru.learning.taskapitests.db.TaskRepository;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class DbSteps {

    private final TaskRepository taskRepository = new TaskRepository();


    @Step("Получить количество задач в БД")
    public long getTasksCount() {
        try {
            return taskRepository.countAll();
        } catch (SQLException e){
            throw new IllegalStateException("Не удалось получить количество задач из БД", e);
        }
    }

    @Step("Проверить, что в БД задач: {expected}")
    public void checkTasksCount(long expected) {
        assertThat(getTasksCount())
                .as("Количество задач в БД")
                .isEqualTo(expected);
    }
    @Step("Получить задачу {taskId} из БД")
    public TaskDbRecord getTaskFromDb(long taskId) {
        try {
            return taskRepository.findById(taskId);
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось получить задачу " + taskId + " из БД", e);
        }
    }

    @Step("Проверить, что задача {taskId} сохранена в БД без искажений")
    public void checkTaskSaved(long taskId, CreateTaskRequest expected){
        TaskDbRecord record =  getTaskFromDb(taskId);
        assertThat(record).as("Запись в БД").isNotNull();
        assertThat(record.title()).as("Название в БД").isEqualTo(expected.getTitle());
        assertThat(record.description()).as("Описание в БД").isEqualTo(expected.getDescription());
        assertThat(record.assignee()).as("Исполнитель в БД").isEqualTo(expected.getAssignee());
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
        try {
            assertThat(taskRepository.countByTitle(title))
                    .as("Количество задач с названием «" + title + "» в БД")
                    .isEqualTo(expected);
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось посчитать задачи с названием " + title, e);
        }
    }

    @Step("Проверить, что сообщение {messageId} учтено как обработанное и привело к задаче {taskId}")
    public void checkMessageProcessed(UUID messageId, long taskId) {
        try {
            assertThat(taskRepository.findTaskIdByMessage(messageId))
                    .as("Задача, привязанная к сообщению " + messageId + " в processed_messages")
                    .isEqualTo(taskId);
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось прочитать processed_messages", e);
        }
    }

    @Step("Проверить, что записей об обработке сообщения {messageId}: {expected}")
    public void checkProcessedCount(UUID messageId, long expected) {
        try {
            assertThat(taskRepository.countProcessedMessages(messageId))
                    .as("Записей в processed_messages для сообщения " + messageId)
                    .isEqualTo(expected);
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось прочитать processed_messages", e);
        }
    }
}
