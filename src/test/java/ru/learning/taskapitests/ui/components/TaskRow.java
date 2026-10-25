package ru.learning.taskapitests.ui.components;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

public class TaskRow {

    private final SelenideElement root;

    public TaskRow(SelenideElement root) {
        this.root = root;
    }

    public SelenideElement root() {
        return root;
    }

    public SelenideElement id() {
        return root.$("[data-testid=task-id]");
    }

    public SelenideElement title() {
        return root.$("[data-testid=task-title]");
    }

    public SelenideElement assignee() {
        return root.$("[data-testid=task-assignee]");
    }

    public SelenideElement priority() {
        return root.$("[data-testid=task-priority]");
    }

    public SelenideElement status() {
        return root.$("[data-testid=task-status]");
    }

    public SelenideElement description() {
        return root.$("[data-testid=task-description]");
    }

    public SelenideElement editButton() {
        return root.$(".actions a.btn");
    }

    public SelenideElement deleteButton(){
        return root.$x(".//form[contains(@action,'/delete')]//button[@type='submit']");
    }

    public String deleteAndConfirm(){
        deleteButton().click();
        return Selenide.confirm();
    }
    public void deleteAndCancel() {
        deleteButton().click();
        Selenide.dismiss();
    }

}
