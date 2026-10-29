package ru.learning.taskapitests.tests.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.NullString;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.Task;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.REGRESSION)
@Tag(Tags.API)
@DisplayName("Запросы создания задач")
public class CreateTaskTests extends TestBase {

    @Tag(Tags.SMOKE)
    @DisplayName("Запрос на создание задачи и её получения по ID ")
    @Test
    void shouldCreateTaskAndGetItByIdWithDto() {
        var request = TaskDataFactory.defaultTaskRequestDTO();

        Response createResponse = step.createTaskDto(request);
        step.checkStatus(createResponse, 201);
        step.checkResponseCreateTaskDataWithDto(createResponse, request);

        long taskId = step.getTaskIdFromResponse(createResponse);
        Task getTask = taskApi.getTaskAsDto(taskId);
        step.checkResponseGetTaskDataWithDto(getTask, request, taskId);
    }

    @Tag(Tags.NEGATIVE)
    @DisplayName("Запрос создания задачи без названия")
    @Test
    void shouldReturn400WhenTitleIsMissing() {
        Response response = step.createTask(TaskDataFactory.invalidCreateTaskRequestWithoutTitle());
        step.checkValidationError(response, "/api/tasks", "title: must not be blank");
    }

    @Tag(Tags.NEGATIVE)
    @Test
    @DisplayName("Запрос создания задачи с некорректным приоритетом")
    void shouldReturn400WhenPriorityIsInvalid() {
        Response response = taskApi.createTask(TaskDataFactory.invalidCreateTaskRequestInvalidPriority());
        step.checkApiError(response, 400,
                "Request body is missing, malformed or contains an invalid value", "/api/tasks");
    }

}
