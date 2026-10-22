package ru.learning.taskapitests.tests;

import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.db.TaskDbRecord;
import ru.learning.taskapitests.db.TaskRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskDatabaseTests extends TestBase {

    @Test
    void shouldSaveCreatedTaskInDatabase() throws Exception {
        long taskId = step.createTaskAndGetId(TaskDataFactory.validCreateTaskRequest());

        TaskDbRecord taskFromDb = new TaskRepository().findById(taskId);

        assertThat(taskFromDb)
                .as("Задача с ID %s должна существовать в БД", taskId)
                .isNotNull();

        assertThat(taskFromDb.id()).isEqualTo(taskId);
        assertThat(taskFromDb.title()).isEqualTo("Автотестовая задача");
        assertThat(taskFromDb.description()).isEqualTo("Создана через REST Assured");
        assertThat(taskFromDb.status()).isEqualTo("NEW");
        assertThat(taskFromDb.priority()).isEqualTo("HIGH");

    }

}
