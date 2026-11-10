package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage {

    public static final String PATH = "/login";

    public LoginPage open() {
        Selenide.open(PATH);
        return this;
    }

    public SelenideElement form() {
        return $("form[action$='/login']");
    }

    public SelenideElement usernameInput() {
        return form().$("#username");
    }

    public SelenideElement passwordInput() {
        return form().$("#password");
    }

    public SelenideElement submitButton() {
        return form().$("button[type=submit]");
    }

    public SelenideElement errorMessage() {
        return $(".err[role=alert]");
    }

    public SelenideElement logoutMessage() {
        return $(".flash[role=status]");
    }

    @Step("Войти как {username}")
    public TasksPage login(String username, String password) {
        usernameInput().setValue(username);
        passwordInput().setValue(password);
        submitButton().click();
        return new TasksPage();
    }
}