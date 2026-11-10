package ru.learning.taskapitests.tests.ui;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.steps.ui.AuthUiSteps;
import ru.learning.taskapitests.ui.BrowserTestBase;
import ru.learning.taskapitests.ui.pages.LoginPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;

@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Epic("UI")
@Feature("Вход в систему")
class LoginUiTests extends BrowserTestBase {

    @Autowired
    private AuthUiSteps authStep;

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Гость, открывший закрытую страницу, попадает на страницу входа")
    void shouldRedirectGuestToLoginPage() {
        new TasksPage().open();

        navStep.checkPageOpened(new LoginPage(), "Вход", "/login");
        navStep.checkNoActiveMenuItem();
    }

    @ParameterizedTest(name = "роль {0}")
    @EnumSource(Role.class)
    @DisplayName("Вход с верными данными: в шапке имя пользователя, ссылки «Войти» нет")
    void shouldLoginWithEachRole(Role role) {
        TasksPage page = authStep.loginThroughForm(role);

        navStep.checkPageOpened(page, "Поиск задач", "/tasks");
        page.header().loginLink().shouldNot(exist);
    }

    @Test
    @Tag(Tags.NEGATIVE)
    @DisplayName("Неверный пароль: остаёмся на странице входа и видим ошибку")
    void shouldShowErrorOnWrongPassword() {
        String username = props.auth().accountFor(Role.USER).username();
        LoginPage page = new LoginPage().open();

        page.login(username, "wrong-password");

        page.errorMessage().shouldHave(text("Неверный логин или пароль"));
        page.header().userName().shouldNot(exist);
    }

    @Test
    @DisplayName("Выход: сообщение о выходе, шапка гостя, закрытые страницы снова недоступны")
    void shouldLogout() {
        TasksPage tasksPage = authStep.loginAs(Role.USER);

        LoginPage loginPage = tasksPage.header().logout();

        loginPage.logoutMessage().shouldHave(text("Вы вышли из системы"));
        loginPage.header().userName().shouldNot(exist);
        new TasksPage().open();
        navStep.checkPageOpened(new LoginPage(), "Вход", "/login");
    }
}