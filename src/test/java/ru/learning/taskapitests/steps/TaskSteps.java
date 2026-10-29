package ru.learning.taskapitests.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.generated.model.ApiErrorResponse;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskSteps {

    private final TaskApiClient taskApi;
    private final List<Long> createdTaskIds = new ArrayList<>();
    public static final Logger log = LoggerFactory.getLogger(TaskSteps.class);

    public static final Map<Integer, String> REASON_PHRASES = Map.of(
            400, "Bad Request",
            404, "Not Found",
            405, "Method Not Allowed",
            409, "Conflict",
            415, "Unsupported Media Type",
            500, "Internal Server Error");


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
    public void checkErrorMessage(Response response, String expectedMessage) {
        assertThat(response.jsonPath().getString("message"))
                .as("Сообщение об ошибке")
                .isEqualTo(expectedMessage);
    }

    @Step("Проверить ответ-ошибку: {expectedStatus}, сообщение «{expectedMessage}»")
    public ApiErrorResponse checkApiError(Response response, int expectedStatus,
                                          String expectedMessage, String expectedPath) {
        ApiErrorResponse error = checkErrorEnvelope(response, expectedStatus, expectedMessage, expectedPath);
        assertThat(error.getDetails()).as("Ошибки по полям (должен быть пустой список)").isEmpty();
        return error;
    }

    @Step("Проверить ошибки валидации по полям")
    public ApiErrorResponse checkValidationError(Response response, String expectedPath,
                                                 String... expectedDetails) {
        ApiErrorResponse error = checkErrorEnvelope(response, 400, "Validation failed", expectedPath);
        assertThat(error.getDetails())
                .as("Ошибки по полям")
                .extracting(detail -> detail.getField() + ": " + detail.getMessage())
                .containsExactlyInAnyOrder(expectedDetails);
        return error;
    }

    private ApiErrorResponse checkErrorEnvelope(Response response, int expectedStatus,
                                                String expectedMessage, String expectedPath) {
        checkStatus(response, expectedStatus);
        ApiErrorResponse error = response.as(ApiErrorResponse.class);

        assertThat(error.getStatus()).as("Поле status").isEqualTo(expectedStatus);
        assertThat(error.getError()).as("Поле error").isEqualTo(REASON_PHRASES.get(expectedStatus));
        assertThat(error.getMessage()).as("Поле message").isEqualTo(expectedMessage);
        assertThat(error.getPath()).as("Поле path").isEqualTo(expectedPath);

        Instant timestamp = Instant.parse(error.getTimestamp());
        assertThat(timestamp)
                .as("Поле timestamp (время ошибки, UTC)")
                .isBetween(Instant.now().minus(Duration.ofMinutes(10)),
                        Instant.now().plus(Duration.ofMinutes(10)));
        return error;
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

    @Step("Проверить, что поле «{field}» в ответе равно null")
    public void checkJsonFieldIsNull(Response response,String field){
        assertThat(response.jsonPath().<Object>get(field))
                .as("Поле " + field + " в ответе")
                .isNull();
    }


}
