package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage {

    public static final String PATH = "/login";

    public LoginPage open() {
        Selenide.open(PATH);
        return this;
    }

    public SelenideElement form() {
        return $("[data-testid=login-form]");
    }

    public SelenideElement usernameInput() {
        return $("[data-testid=login-username]");
    }

    public SelenideElement passwordInput() {
        return $("[data-testid=login-password]");
    }

    public SelenideElement submitButton() {
        return $("[data-testid=login-submit]");
    }
}