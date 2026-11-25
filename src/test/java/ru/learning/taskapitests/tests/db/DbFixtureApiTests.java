package ru.learning.taskapitests.tests.db;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.db.TaskDbRecord;
import ru.learning.taskapitests.extensions.WithDbTask;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.learning.taskapitests.generated.model.CreateTaskRequest.PriorityEnum.HIGH;
import static ru.learning.taskapitests.generated.model.CreateTaskRequest.StatusEnum.DONE;
import static ru.learning.taskapitests.steps.TaskSteps.TASK_SCHEMA;

@Tag(Tags.DB)
@Tag(Tags.REGRESSION)
@Epic("БД")
@Feature("Данные, созданные напрямую в БД")
public class DbFixtureApiTests extends TestBase {


    @Test
    @WithDbTask(status = DONE, priority = HIGH, createdDaysAgo = 365)
    @DisplayName("Старая задача из БД видна через API")
    void taskCreatedInDbIsVisibleViaApi(TaskDbRecord task) {
        Response response = step.getTask(task.id());

        step.checkStatus(response, 200);
        step.checkMatchesSchema(response, TASK_SCHEMA);
        step.checkTaskData(response, task.id(), task.title(), task.status(), task.priority());
        assertThat(response.jsonPath().getString("assignee")).as("Поле assignee").isEqualTo(task.assignee());
        assertThat(response.jsonPath().getString("description")).as("Поле description").isEqualTo(task.description());

        LocalDateTime createdAt = LocalDateTime.parse(response.jsonPath().getString("createdAt"));
        assertThat(createdAt).as("Дата создания совпадает с БД").isEqualTo(task.createdAt());
        assertThat(createdAt).as("Дата создания старше года").isBefore(LocalDateTime.now().minusDays(364));
    }

    @Test
    @WithDbTask
    @DisplayName("Задача, удалённая напрямую в БД, через API больше не находится (404)")
    void taskDeletedInDbNotFoundViaApi(TaskDbRecord task) {
        dbStep.deleteTaskInDb(task.id());

        Response response = step.getTask(task.id());

        step.checkApiError(response,404, "Task not found with id: " + task.id(),  "/api/tasks/" + task.id());
    }


}
