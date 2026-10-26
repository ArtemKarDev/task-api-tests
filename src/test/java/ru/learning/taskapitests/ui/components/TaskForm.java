package ru.learning.taskapitests.ui.components;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class TaskForm {

    private final SelenideElement root = $("[data-testid=task-form]");

    public SelenideElement titleInput() {
        return root.$("[data-testid=title-input]");
    }

    public SelenideElement descriptionInput() {
        return root.$x(".//label[normalize-space()='Описание']/following-sibling::textarea");
    }

    public SelenideElement assigneeInput() {
        return root.$("#assignee");
    }

    public SelenideElement prioritySelect() {
        return root.$("[data-testid=priority-select]");
    }

    public SelenideElement statusSelect() {
        return root.$("[data-testid=status-select]");
    }

    public SelenideElement titleError() {
        return root.$("[data-testid=title-error]");
    }

    public SelenideElement descriptionError() {
        return root.$("[data-testid=description-error]");
    }

    public SelenideElement assigneeError() {
        return root.$("[data-testid=assignee-error]");
    }

    public SelenideElement priorityError() {
        return root.$("[data-testid=priority-error]");
    }

    public SelenideElement statusError() {
        return root.$("[data-testid=status-error]");
    }

    public ElementsCollection errors() {
        return root.$$("[data-testid$='-error']");
    }

    public SelenideElement submitButton() {
        return root.$("[data-testid=submit-btn]");
    }

    public SelenideElement cancelButton() {
        return root.$("[data-testid=cancel-btn]");
    }

    public TaskForm fillTitle(String title) {
        titleInput().setValue(title);
        return this;
    }

    public TaskForm fillDescription(String description) {
        descriptionInput().setValue(description);
        return this;
    }

    public TaskForm fillAssignee(String assignee) {
        assigneeInput().setValue(assignee);
        return this;
    }

    public TaskForm selectPriority(String value) {
        prioritySelect().selectOptionByValue(value);
        return this;
    }

    public TaskForm selectStatus(String value) {
        statusSelect().selectOptionByValue(value);
        return this;
    }

    public void submit() {
        submitButton().click();
    }


}