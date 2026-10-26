package ru.learning.taskapitests.steps.ui;

import io.qameta.allure.Step;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.steps.TaskSteps;
import ru.learning.taskapitests.ui.components.TaskRow;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.Condition.*;

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

        row.priority().shouldHave(exactText(expected.getPriority().toString()));
        row.status().shouldHave(exactText(expected.getStatus().toString()));
    }

    @Step("Проверить что задачи с ID {taskId} нет в списке")
    public void checkTaskNotInList(TasksPage page, long taskId) {
        page.rowById(taskId).root().shouldNot(exist);
    }

    @Step("Создать задачу через форму: {request.title}")
    public TasksPage createTaskViaForm(CreateTaskPage page, CreateTaskRequest request) {
        page.form()
                .fillTitle(request.getTitle())
                .fillAssignee(request.getAssignee())
                .selectPriority(request.getPriority().toString())
                .selectStatus(request.getStatus().toString());

        if (request.getDescription() != null) {
            page.form().fillDescription(request.getDescription());
        }
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

    @Step("Проверит сообщение: {expectedMessage}")
    public void checkFlashMessage(TasksPage page, String expectedMessage) {
        page.flash().shouldHave(exactText(expectedMessage));
    }
}
