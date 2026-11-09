package ru.learning.taskapitests.tests.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.extensions.WithTask;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.Task;
import static ru.learning.taskapitests.utils.TimeComparators.SAME_MOMENT;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.REGRESSION)
@Tag(Tags.API)
@DisplayName("Запросы получения задач")
public class GetTaskTests extends TestBase {

    @Test
    @WithTask(status = CreateTaskRequest.StatusEnum.IN_PROGRESS)
    @DisplayName("Получение задачи по id")
    public void getTaskById(Task task){
        Response response = step.getTask(task.getId());
        step.checkStatus(response, 200);
        step.checkTaskData(response, task.getId(),task.getTitle(),task.getStatus().name(),task.getPriority().name());
        assertThat(response.as(Task.class))
                .as("Задача из ответа на создание и из ответа на get одна и таже")
                .usingRecursiveComparison()
                .withComparatorForFields(SAME_MOMENT, "createdAt", "updatedAt")
                .isEqualTo(task);
    }

    @Test
    @Tag(Tags.NEGATIVE)
    public void shouldReturn404WhenTaskDoesNotExist() {
        long nonexistentId = Long.MAX_VALUE;
        Response response = step.getTask(nonexistentId);
        step.checkApiError(response,404, "Task not found with id: " + nonexistentId, "/api/tasks/" + nonexistentId);
    }



}
