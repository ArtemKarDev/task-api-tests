package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

/** Страница смены своего пароля. */
public class ProfilePasswordPage {

    public static final String PATH = "/profile/password";

    @Step("Открыть страницу смены пароля")
    public ProfilePasswordPage open() {
        Selenide.open(PATH);
        return this;
    }

    public SelenideElement form() {
        return $("form[action$='/profile/password']");
    }

    public SelenideElement currentPasswordInput() {
        return form().$("#currentPassword");
    }

    public SelenideElement newPasswordInput() {
        return form().$("#newPassword");
    }

    public SelenideElement confirmPasswordInput() {
        return form().$("#confirmPassword");
    }

    /** Общая ошибка формы, не привязанная к полю. */
    public SelenideElement formError() {
        return form().$(".err[role=alert]");
    }

    /** Ошибка конкретного поля: блок .err сразу после поля. */
    public SelenideElement fieldError(String fieldId) {
        return form().$("#" + fieldId + " + .err");
    }

    @Step("Сменить пароль: отправить форму")
    public ProfilePasswordPage changePassword(String current, String newPassword) {
        currentPasswordInput().setValue(current);
        newPasswordInput().setValue(newPassword);
        confirmPasswordInput().setValue(newPassword);
        form().$("button[type=submit]").click();
        return this;
    }
}