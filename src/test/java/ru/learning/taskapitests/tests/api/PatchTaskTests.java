package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.extensions.WithTask;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.PatchTaskRequest;
import ru.learning.taskapitests.generated.model.Task;
import static ru.learning.taskapitests.utils.TimeComparators.SAME_MOMENT;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
@Tag(Tags.REGRESSION)
@Tag(Tags.API)
@DisplayName("Запросы частичного обновления задач")
public class PatchTaskTests extends TestBase {

    @Test
    @WithTask(status = CreateTaskRequest.StatusEnum.NEW)
    @DisplayName("PATCH меняет статус и не трогает остальные поля")
    void patchStatus(Task task) {
        Response response = taskApi.patchTaskDto(
                task.getId(),
                new PatchTaskRequest().status(PatchTaskRequest.StatusEnum.DONE));

        assertThat(response.statusCode()).as("Код ответа").isEqualTo(200);
        Task actual = response.as(Task.class);
        assertThat(actual.getStatus()).as("Статус").isEqualTo(Task.StatusEnum.DONE);
        assertThat(actual.getTitle()).as("Название не изменилось").isEqualTo(task.getTitle());
        assertThat(actual.getPriority()).as("Приоритет не изменился").isEqualTo(task.getPriority());
    }

    @Test
    @Tag(Tags.SMOKE)
    @WithTask(status = CreateTaskRequest.StatusEnum.DONE, priority = CreateTaskRequest.PriorityEnum.HIGH)
    @DisplayName("PATCH меняет только priority, остальные поля остаются прежними")
    void shouldPatchOnlyPriorityAndKeepOtherFields(Task task) {
        PatchTaskRequest request = new PatchTaskRequest()
                .priority(PatchTaskRequest.PriorityEnum.LOW);

        Response patchResponse = taskApi.patchTaskDto(task.getId(), request);
        patchResponse.then().statusCode(200);
        Task patched = patchResponse.as(Task.class);

        Response getResponse = taskApi.getTaskById(task.getId());
        getResponse.then().statusCode(200);
        Task persisted = getResponse.as(Task.class);

        checkOnlyPriorityChanged(patched, task, "в ответе PATCH");
        checkOnlyPriorityChanged(persisted, task, "после повторного GET");

        assertThat(LocalDateTime.parse(patched.getUpdatedAt()))
                .as("updatedAt обновился после PATCH")
                .isAfter(LocalDateTime.parse(task.getUpdatedAt()));
    }

    @Step("Проверить, что изменился только priority ({where})")
    private void checkOnlyPriorityChanged(Task actual, Task original, String where) {
        assertThat(actual.getPriority())
                .as("Новый priority " + where)
                .isEqualTo(Task.PriorityEnum.LOW);

        assertThat(actual)
                .as("Остальные поля не изменились " + where)
                .usingRecursiveComparison()
                .withComparatorForFields(SAME_MOMENT, "createdAt")
                .ignoringFields("priority", "updatedAt")
                .isEqualTo(original);
    }

    @Test
    @Tag(Tags.NEGATIVE)
    void shouldReturn404WhenNonexistentTask() {
        long taskId = Long.MAX_VALUE;
        Response patchResponse = taskApi.patchTask(taskId, TaskDataFactory.validPatchPriorityRequestBody());
        step.checkApiError(patchResponse, 404, "Task not found with id: " + taskId, "/api/tasks/" + taskId);
    }

    @Test
    @Tag(Tags.NEGATIVE)
    void shouldReturn400WhenPatchBodyIsEmpty() {
        taskApi.patchTask(1, TaskDataFactory.emptyRequestBody()).then().statusCode(400);
    }
}
