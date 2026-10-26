package ru.learning.taskapitests.steps;

import io.qameta.allure.Step;
import ru.learning.taskapitests.db.TaskRepository;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class DbSteps {

    private final TaskRepository taskRepository = new TaskRepository();


    @Step("Получить количество задач в БД")
    public long getTasksCount() throws SQLException {
        return taskRepository.countAll();

    }

    @Step("Проверить, что в БД задач: {expected}")
    public void checkTasksCount(long expected) throws SQLException {
        assertThat(getTasksCount())
                .as("Количество задач в БД")
                .isEqualTo(expected);
    }
}
