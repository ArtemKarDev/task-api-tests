package ru.learning.taskapitests.tests.api;


import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.client.DirectoryStub;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.extensions.TestEmployee;
import ru.learning.taskapitests.extensions.WithEmployee;
import ru.learning.taskapitests.extensions.WithTask;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.PatchTaskRequest;
import ru.learning.taskapitests.generated.model.Task;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.learning.taskapitests.data.TaskDataFactory.uniqueTitle;

@Tag(Tags.STUB)
@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Epic("Интеграция с справочником")
@Feature("Проверка сотрудника в справочнике")
@DisplayName("Проверка сотрудника в справочнике")
public class TaskAssigneeDirectoryTests extends TestBase {


    @Autowired
    private DirectoryStub directoryStub;

    private static final String DIRECTORY_UNAVAILABLE =
            "Employee directory is temporarily unavailable, try again later";

    private static final long SLOW_STUB_DELAY_MS = 5000;

    @Test
    @WithEmployee(WithEmployee.State.EXISTS)
    @DisplayName("Создание задачи с известным исполнителем возвращает 201")
    void shouldReturn201WithAssigneeExist(TestEmployee employee) {
        CreateTaskRequest request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(uniqueTitle("assignee exists"));
            r.assignee(employee.login());
        });

        Response response = taskApi.createTaskDto(request);
        step.checkStatus(response, 201);
        step.checkResponseCreateTaskDataWithDto(response, request);

        long taskId = step.getTaskIdFromResponse(response);
        Task getTask = taskApi.getTaskAsDto(taskId);
        step.checkResponseGetTaskDataWithDto(getTask, request, taskId);

        List<LoggedRequest> lookups = directoryStub.lookups(employee.login());
        assertThat(lookups).as("запросов к справочнику").hasSize(1);
        assertThat(lookups.getFirst().getHeader("Authorization"))
                .as("сервис передал токен справочнику")
                .startsWith("Bearer ");
    }

    @Test
    @WithEmployee(WithEmployee.State.NOT_FOUND)
    @DisplayName("Создание задачи с неизвестным исполнителем возвращает 422")
    void shouldRejectUnknownAssignee(TestEmployee employee) {
        CreateTaskRequest request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(uniqueTitle("Unknown assignee"));
            r.assignee(employee.login());
        });

        Response response = taskApi.createTaskDto(request);
        step.checkStatus(response, 422);
        step.checkErrorMessage(response, "Исполнитель не найден в справочнике сотрудников: " + employee.login());

        assertThat(directoryStub.lookups(employee.login())).as("запросов к справочнику").hasSize(1);

    }

    @Test
    @WithEmployee(WithEmployee.State.DIRECTORY_ERROR)
    @DisplayName("Создание задачи при недоступном справочнике возвращает 503")
    void shouldReturn503WithDirectoryNotAvailable(TestEmployee employee) {
        CreateTaskRequest request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(uniqueTitle("directory_not_available"));
            r.assignee(employee.login());
        });

        Response response = taskApi.createTaskDto(request);
        step.checkStatus(response, 503);
        step.checkErrorMessage(response, DIRECTORY_UNAVAILABLE);

        assertThat(directoryStub.lookups(employee.login())).as("запросов к справочнику").hasSize(1);

    }

    @Test
    @WithEmployee(WithEmployee.State.DIRECTORY_SLOW)
    @DisplayName("Создание задачи при медленном ответе справочника возвращает 503 по таймауту")
    void shouldReturn503WithDirectorySlowResponse(TestEmployee employee) {
        CreateTaskRequest request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(uniqueTitle("directory slow response"));
            r.assignee(employee.login());
        });

        Response response = taskApi.createTaskDto(request);

        assertThat(response.time())
                .as("сервис прервал ожидание по своему таймауту, не дождавшись заглушки")
                .isLessThan(SLOW_STUB_DELAY_MS);
        assertThat(directoryStub.lookups(employee.login())).as("запросов к справочнику").hasSize(1);

    }

    @Test
    @WithTask
    @WithEmployee(WithEmployee.State.NOT_FOUND)
    @DisplayName("Частичное обновление с неизвестным исполнителем возвращает 422 и не меняет задачу")
    void shouldRejectPatchUnknownAssignee(TestEmployee employee, Task task) {
        CreateTaskRequest request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(uniqueTitle("patch unknown assignee"));
            r.assignee(employee.login());
        });

        Response response = taskApi.patchTaskDto(
                task.getId(),
                new PatchTaskRequest()
                        .status(PatchTaskRequest.StatusEnum.DONE)
                        .assignee(employee.login()));

        step.checkStatus(response, 422);
        step.checkErrorMessage(response, "Исполнитель не найден в справочнике сотрудников: " + employee.login());
        Task afterPatch = taskApi.getTaskAsDto(task.getId());
        assertThat(afterPatch.getAssignee()).as("исполнитель не изменился").isEqualTo(task.getAssignee());
        assertThat(afterPatch.getStatus()).as("статус не изменился").isEqualTo(task.getStatus());


        assertThat(directoryStub.lookups(employee.login())).as("запросов к справочнику").hasSize(1);

    }

    @Test
    @WithEmployee(WithEmployee.State.EXISTS)
    @DisplayName("Обновление без смены исполнителя не обращается к справочнику")
    void shouldNotCallDirectoryWhenAssigneeUnchanged(TestEmployee employee) {
        CreateTaskRequest request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(uniqueTitle("assignee unchanged"));
            r.assignee(employee.login());
        });
        Response created = taskApi.createTaskDto(request);
        step.checkStatus(created, 201);
        long taskId = step.getTaskIdFromResponse(created);

        Response patched = taskApi.patchTaskDto(taskId,
                new PatchTaskRequest().title(uniqueTitle("renamed")));
        step.checkStatus(patched, 200);

        assertThat(directoryStub.lookups(employee.login()))
                .as("запрос к справочнику был только при создании")
                .hasSize(1);
    }


}
