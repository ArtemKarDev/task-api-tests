package ru.learning.taskapitests.steps.ui;

import io.qameta.allure.Step;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.ui.components.TaskRow;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;

public class TaskUiSteps {

    @Step("Проверить в списке задачу с ID {taskId}")
    public void checkTaskInList(TasksPage page, long taskId, CreateTaskRequest expected) {
        TaskRow row = page.rowById(taskId);
        row.title().shouldHave(exactText(expected.getTitle()));
        row.description().shouldHave(exactText(expected.getDescription()));
        row.assignee().shouldHave(exactText(expected.getAssignee()));
        row.priority().shouldHave(exactText(expected.getPriority().toString()));
        row.status().shouldHave(exactText(expected.getStatus().toString()));
    }

    @Step("Проверить что задачи с ID {taskId} нет в списке")
    public void checkTaskNotInList(TasksPage page, long taskId){
        page.rowById(taskId).root().shouldNot(exist);
    }
}
