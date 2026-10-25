package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.Selenide;
import ru.learning.taskapitests.ui.components.TaskForm;

public class EditTaskPage extends BasePage {

    private final TaskForm form = new TaskForm();

    public EditTaskPage open(long taskId) {
        Selenide.open("/tasks/" + taskId + "/edit");
        return this;
    }

    public TaskForm form() {
        return form;
    }

    public TasksPage submitValid() {
        form.submit();
        return new TasksPage();
    }

    public EditTaskPage submitInvalid() {
        form.submit();
        return this;
    }

    public TasksPage cancel() {
        form.cancelButton().click();
        return new TasksPage();
    }
}