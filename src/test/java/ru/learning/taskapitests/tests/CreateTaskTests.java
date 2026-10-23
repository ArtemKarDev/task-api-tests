package ru.learning.taskapitests.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.Task;

@Tag("regress")
@Tag("api_tests")
public class CreateTaskTests extends TestBase {

    @Tag("smoke")
    @DisplayName("Отправить запрос на создание задачи и её получения по ID ")
    @Test
    void shouldCreateTaskAndGetItByIdWithDto() {
        Response createResponse = step.createTaskDto(TaskDataFactory.defaultTaskRequestDTO());
        step.checkResponseCreateTaskDataWithDto(createResponse, TaskDataFactory.defaultTaskRequestDTO());
        Task getTask = taskApi.getTaskAsDto(createResponse.jsonPath().getLong("id"));
        step.checkResponseGetTaskDataWithDto(getTask, TaskDataFactory.defaultTaskRequestDTO(),
                createResponse.jsonPath().getLong("id"));
    }

    @Tag("negative")
    @DisplayName("Запрос создания задачи без названия")
    @Test
    void shouldReturn400WhenTitleIsMissing() {
        Response response = step.createTask(TaskDataFactory.invalidCreateTaskRequestWithoutTitle());
        step.checkStatus(response, 400);
    }

    @Tag("negative")
    @Test
    @DisplayName("Запрос создания задачи с некорректным приоритетом")
    void shouldReturn400WhenPriorityIsInvalid() {
        Response response = taskApi.createTask(TaskDataFactory.invalidCreateTaskRequestInvalidPriority());
        step.checkStatus(response, 400);
    }

}
