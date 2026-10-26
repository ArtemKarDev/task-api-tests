package ru.learning.taskapitests.tests.ui;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.ui.UiTestBase;

@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Epic("UI")
@Feature("Создание задачи")
public class CreateTaskUiTests extends UiTestBase {

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Задача, созданная через форму, видна в списке и сохранена в системе")
    void shouldCreateTaskViaForm() {
        var request = TaskDataFactory.defaultTaskRequestDTO();

        long taskId = uiStep.createTaskViaUi(navStep.openCreateTaskPage(), request);
        Response response = step.getTask(taskId);
        step.checkStatus(response, 200);
        step.checkResponseCreateTaskDataWithDto(response, request);
    }

    @ParameterizedTest(name = "приоритет {0}")
    @EnumSource(CreateTaskRequest.PriorityEnum.class)
    @DisplayName("Задача создаётся с каждым приоритетом")
    void shouldCreateTaskWithEachPriority(CreateTaskRequest.PriorityEnum priority){
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.priority(priority));

        uiStep.createTaskViaUi(navStep.openCreateTaskPage(), request);
    }
    @ParameterizedTest(name = "статус {0}")
    @EnumSource(CreateTaskRequest.StatusEnum.class)
    @DisplayName("Задача создаётся с каждым статусом")
    void shouldCreateTaskWithEachStatus(CreateTaskRequest.StatusEnum status) {
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.status(status));

        uiStep.createTaskViaUi(navStep.openCreateTaskPage(), request);
    }

    @Test
    @DisplayName("Описание необязательно: задача создаётся без него")
    void shouldCreateTaskWithoutDescription() {
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.description(null));

        uiStep.createTaskViaUi(navStep.openCreateTaskPage(), request);
    }

    @ParameterizedTest(name = "название «{0}»")
    @ValueSource(strings = {
            "Задача №1 «кириллица»",
            "<b>жирный</b> & <i>курсив</i>",
            "Кавычки \"двойные\" и 'одинарные'"
    })
    @DisplayName("Спецсимволы в названии сохраняются и экранируются при показе")
    void shouldCreateTaskWithSpecialCharsInTitle(String titlePrefix) {
        var request = TaskDataFactory.customTaskRequestDTO(
                r -> r.title(TaskDataFactory.uniqueTitle(titlePrefix)));
        uiStep.createTaskViaUi(navStep.openCreateTaskPage(), request);

    }

}
