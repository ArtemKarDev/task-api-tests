package ru.learning.taskapitests.steps;

import io.qameta.allure.Step;
import ru.learning.taskapitests.db.TaskDbRecord;
import ru.learning.taskapitests.db.TaskRepository;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

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
    }
}
