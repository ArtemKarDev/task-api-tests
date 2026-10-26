package ru.learning.taskapitests.tests.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.db.TaskDbRecord;
import ru.learning.taskapitests.db.TaskRepository;

import static org.assertj.core.api.Assertions.assertThat;
@Tag(Tags.DB)
@DisplayName("Тесты процедур в БД")
public class TaskDatabaseTests extends TestBase {
    @Tag("smoke")
    @DisplayName("Отправить запрос на создание задачи и проверить БД")
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
