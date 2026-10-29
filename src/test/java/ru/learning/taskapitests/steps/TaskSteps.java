package ru.learning.taskapitests.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskSteps {

    private final TaskApiClient taskApi;
    private final List<Long> createdTaskIds = new ArrayList<>();

    public static final Logger log = LoggerFactory.getLogger(TaskSteps.class);

    public TaskSteps(TaskApiClient taskApi) {
        this.taskApi = taskApi;
    }

    @Step("Получить задачу по ID: {taskId}")
    public Response getTask(long taskId) {
        return taskApi.getTaskById(taskId);
    }

    @Step("Удалить задачу по ID: {taskId}")
    public Response deleteTask(long taskId) {
        return taskApi.deleteTask(taskId);
    }

    @Step("Проверить HTTP-статус: {expectedStatus}")
    public void checkStatus(Response response, int expectedStatus) {
        assertThat(response.statusCode())
                .as("HTTP-статус ответа").isEqualTo(expectedStatus);
    }


    @Step("Создать задачу")
    public Response createTask(String requestBody) {
        return registerForCleanup(taskApi.createTask(requestBody));
    }

    @Step("Создать задачу и получить её ID")
    public long createTaskAndGetId(String requestBody) {
        return createTask(requestBody).jsonPath().getLong("id");
    }

    @Step("Создать задачу с помощью DTO и получить response")
    public Response createTaskDto(CreateTaskRequest request) {
        return registerForCleanup(taskApi.createTaskDto(request));
    }

//    @Step("Создать задачу, ожидая ошибку")
//    public ApiErrorResponse createTaskExpectingError(){
//
//    }

    @Step("Проверить данные задачи")
    public void checkTaskData(Response response,
                              long expectedId,
                              String expectedTitle,
                              String expectedStatus,
                              String expectedPriority) {
        assertThat(response.jsonPath().getLong("id"))
                .as("ID задачи")
                .isEqualTo(expectedId);

        assertThat(response.jsonPath().getString("title"))
                .as("Название задачи")
                .isEqualTo(expectedTitle);

        assertThat(response.jsonPath().getString("status"))
                .as("Статус задачи")
                .isEqualTo(expectedStatus);

        assertThat(response.jsonPath().getString("priority"))
                .as("Приоритет задачи")
                .isEqualTo(expectedPriority);
    }

    @Step("Проверить данные задачи")
    public void checkResponseCreateTaskDataWithDto(Response response, CreateTaskRequest request) {
        assertThat(response.jsonPath().getLong("id"))
                .as("ID задачи")
                .isPositive();

        assertThat(response.jsonPath().getString("title"))
                .as("Название задачи")
                .isEqualTo(request.getTitle());

        assertThat(response.jsonPath().getString("status"))
                .as("Статус задачи")
                .isEqualTo(request.getStatus().toString());

        assertThat(response.jsonPath().getString("priority"))
                .as("Приоритет задачи")
                .isEqualTo(request.getPriority().toString());
    }

    @Step("Проверить данные задачи")
    public void checkResponseGetTaskDataWithDto(Task task, CreateTaskRequest request, Long taskId) {
        assertThat(task.getId())
                .as("ID задачи")
                .isEqualTo(taskId);

        assertThat(task.getTitle())
                .as("Название задачи")
                .isEqualTo(request.getTitle());

        assertThat(task.getStatus())
                .as("Статус задачи")
                .isEqualTo(Task.StatusEnum.valueOf(request.getStatus().name()));

        assertThat(task.getPriority())
                .as("Приоритет задачи")
                .isEqualTo(Task.PriorityEnum.valueOf(request.getPriority().name()));
    }

    @Step("Проверить текст ошибки {expectedError}")
    public void checkErrorText(Response response, String expectedError) {
        assertThat(response.jsonPath().getString("error"))
                .as("текст ответа c ошибкой ").isEqualTo(expectedError);
    }

    public void clearTaskIdInCreatedList() {
        createdTaskIds.clear();
    }

    public void cleanDbCreatedTasks() {
        log.info("Очистка БД, задачи на удаление: {}", createdTaskIds);
        for (Long taskId : createdTaskIds) {
            int statusCode =
                    deleteTask(taskId).then().extract().statusCode();
            if (statusCode != 204 && statusCode != 404) {
                throw new AssertionError("Неудалось удалить задачу с ID " + taskId + " HTTP-статус: " + statusCode);
            }
        }
        createdTaskIds.clear();
    }

    public void registerTaskForCleanup(Long taskId) {
        createdTaskIds.add(taskId);
    }

    public Long getTaskIdFromResponse(Response response) {
        return response.jsonPath().getLong("id");
    }


    private Response registerForCleanup(Response response) {
        if (response.statusCode() == 201) {
            registerTaskForCleanup(response.jsonPath().getLong("id"));
        }
        return response;
    }


}
