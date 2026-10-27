package ru.learning.taskapitests.steps.ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.steps.TaskSteps;
import ru.learning.taskapitests.ui.components.TaskForm;
import ru.learning.taskapitests.ui.components.TaskRow;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskUiSteps {

    private final TaskSteps apiSteps;

    public TaskUiSteps(TaskSteps apiSteps) {
        this.apiSteps = apiSteps;
    }


    @Step("Проверить в списке задачу с ID {taskId}")
    public void checkTaskInList(TasksPage page, long taskId, CreateTaskRequest expected) {
        TaskRow row = page.rowById(taskId);
        row.title().shouldHave(exactText(expected.getTitle()));

        if (expected.getDescription() == null) {
            row.description().shouldBe(empty);
        } else {
            row.description().shouldHave(exactText(expected.getDescription()));
        }
        if (expected.getAssignee() == null) {
            row.assignee().shouldBe(empty);
        } else {
            row.assignee().shouldHave(exactText(expected.getAssignee()));
        }
        row.assignee().shouldHave(exactText(expected.getAssignee()));
        row.priority().shouldHave(exactText(expected.getPriority().toString()));
        row.status().shouldHave(exactText(expected.getStatus().toString()));
    }

    @Step("Проверить что задачи с ID {taskId} нет в списке")
    public void checkTaskNotInList(TasksPage page, long taskId) {
        page.rowById(taskId).root().shouldNot(exist);
    }

    @Step("Создать задачу через форму: {request.title}")
    public TasksPage createTaskViaForm(CreateTaskPage page, CreateTaskRequest request) {
        fillForm(page.form(), request);
        return page.submitValid();
    }

    @Step("Создать задачу через UI: {request.title}")
    public long createTaskViaUi(CreateTaskPage page,CreateTaskRequest request){
        TasksPage tasksPage = createTaskViaForm(page, request);

        long taskId = getTaskIdFromList(tasksPage, request.getTitle());
        apiSteps.registerTaskForCleanup(taskId);

        checkFlashMessage(tasksPage, "Задача создана");
        checkTaskInList(tasksPage, taskId, request);
        return taskId;
    }

    @Step("Прочитать ID задачи {title} из списка")
    public Long getTaskIdFromList(TasksPage page, String title) {
        return Long.parseLong(page.rowByTitle(title).id().getText().trim());
    }

    @Step("Проверить сообщение: {expectedMessage}")
    public void checkFlashMessage(TasksPage page, String expectedMessage) {
        page.flash().shouldHave(exactText(expectedMessage));
    }

    private void fillForm(TaskForm form, CreateTaskRequest request){
        if (request.getTitle() != null) {form.fillTitle(request.getTitle());}
        if (request.getDescription() != null) {form.fillDescription(request.getDescription());}
        if (request.getAssignee() != null) {form.fillAssignee(request.getAssignee());}
        if (request.getStatus() != null) {form.selectStatus(request.getStatus().toString());}
        if (request.getPriority() != null) {form.selectPriority(request.getPriority().toString());}
    }

    @Step("Отправить форму с некорректными данными")
    public CreateTaskPage submitFormWithErrors(CreateTaskPage page,CreateTaskRequest request){
        fillForm(page.form(), request);
        return page.submitInvalid();
    }


    @Step("Проверить сообщение об ошибке: {expectedMessage}")
    public void checkFieldError(SelenideElement errorElement, String expectedMessage){
        errorElement.shouldHave(exactText(expectedMessage));
    }

    @Step("Проверить количество ошибок на форме: {expectedCount}")
    public void checkFormErrorsCount(CreateTaskPage page,int expectedCount){
        page.form().errors().shouldHave(size(expectedCount));
    }

}
