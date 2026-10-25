package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.Selenide;
import ru.learning.taskapitests.ui.components.TaskForm;

public class CreateTaskPage extends BasePage {

    public static final String PATH = "/tasks/new";

    private final TaskForm form = new TaskForm();

    public CreateTaskPage open() {
        Selenide.open(PATH);
        return this;
    }

    public TaskForm form() {
        return form;
    }

    public TasksPage submitValid() {
        form.submit();
        return new TasksPage();
    }


    public CreateTaskPage submitInvalid() {
        form.submit();
        return this;
    }

    public TasksPage cancel() {
        form.cancelButton().click();
        return new TasksPage();
    }
}
