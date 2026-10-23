package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.Task;

public class CreateTaskTests extends TestBase {

    @Test
    void shouldCreateTaskAndGetItByIdWithDto() {
        Response createResponse = step.createTaskDto(TaskDataFactory.defaultTaskRequestDTO());
        step.checkResponseCreateTaskDataWithDto(createResponse, TaskDataFactory.defaultTaskRequestDTO());
        Task getTask = taskApi.getTaskAsDto(createResponse.jsonPath().getLong("id"));
        step.checkResponseGetTaskDataWithDto(getTask, TaskDataFactory.defaultTaskRequestDTO(),
                createResponse.jsonPath().getLong("id"));
    }

    @Test
    void shouldReturn400WhenTitleIsMissing() {
        Response response = step.createTask(TaskDataFactory.invalidCreateTaskRequestWithoutTitle());
        step.checkStatus(response, 400);
    }

    @Test
    @DisplayName("Запрос создания задачи с некорректным приоритетом")
    void shouldReturn400WhenPriorityIsInvalid() {
        Response response = taskApi.createTask(TaskDataFactory.invalidCreateTaskRequestInvalidPriority());
        step.checkStatus(response, 400);
    }

}
