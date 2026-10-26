package ru.learning.taskapitests.tests.ui;


import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.ui.UiTestBase;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;

import java.sql.SQLException;

import static com.codeborne.selenide.Condition.exactText;

@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Tag(Tags.NEGATIVE)
@Epic("UI")
@Feature("Валидация формы создания")
public class CreateTaskValidationUiTests extends UiTestBase {


    @ParameterizedTest(name = "название: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", " "})
    @DisplayName("Пустое или пробельное название не принимается")
    void shouldVisibleErrorCreateInvalidTitle(String title) throws SQLException {

        var request = TaskDataFactory.customTaskRequestDTO(r -> r.title(title));

        CreateTaskPage page = submitInvalidAndCheckNothingSaved(request,1);

        uiStep.checkFieldError(page.form().titleError(), "Введите название задачи");

    }

    @ParameterizedTest(name = "исполнитель: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", " "})
    @DisplayName("Пустой или пробельный исполнитель не принимается")
    void shouldVisibleErrorCreateInvalidAssignee(String assignee) throws SQLException {

        var request = TaskDataFactory.customTaskRequestDTO(r -> r.assignee(assignee));

        CreateTaskPage page = submitInvalidAndCheckNothingSaved(request,1);

        uiStep.checkFieldError(page.form().assigneeError(), "Укажите исполнителя");

    }

    @Test

    @DisplayName("Форма без приоритета не принимается")
    void shouldVisibleErrorCreateInvalidPriority() throws SQLException {
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.priority(null));
        CreateTaskPage page = submitInvalidAndCheckNothingSaved(request,1);
        uiStep.checkFieldError(page.form().priorityError(), "Выберите приоритет");

    }
    @Test
    @DisplayName("Форма без статуса не принимается")
    void shouldRejectMissingStatus() throws SQLException {
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.status(null));
        CreateTaskPage page = submitInvalidAndCheckNothingSaved(request, 1);
        uiStep.checkFieldError(page.form().statusError(), "Выберите статус");
    }
    @Test
    @DisplayName("Пустая форма создания показывает ошибки у обязательных полей")
    void shouldShowErrorsForEmptyForm() throws SQLException {
        var request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(null);
            r.assignee(null);
            r.priority(null);
            r.status(null);
        });
        CreateTaskPage page = submitInvalidAndCheckNothingSaved(request, 4);

        uiStep.checkFieldError(page.form().titleError(),"Введите название задачи");
        uiStep.checkFieldError(page.form().assigneeError(),"Укажите исполнителя");
        uiStep.checkFieldError(page.form().priorityError(),"Выберите приоритет");
        uiStep.checkFieldError(page.form().statusError(), "Выберите статус");
    }

    private CreateTaskPage submitInvalidAndCheckNothingSaved(CreateTaskRequest request, int expectedErrors) throws SQLException {
        long before = dbSteps.getTasksCount();

        CreateTaskPage page = uiStep.submitFormWithErrors(navStep.openCreateTaskPage(), request);

        navStep.checkPageOpened(page,"Создание задачи", "/tasks");
        uiStep.checkFormErrorsCount(page, expectedErrors);
        dbSteps.checkTasksCount(before);
        return page;
    }

}
