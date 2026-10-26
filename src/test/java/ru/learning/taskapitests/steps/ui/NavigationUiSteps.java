package ru.learning.taskapitests.steps.ui;

import io.qameta.allure.Step;
import ru.learning.taskapitests.ui.components.Header;
import ru.learning.taskapitests.ui.pages.*;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class NavigationUiSteps {

    @Step("Открыть страницу «Поиск задач»")
    public TasksPage openTasksPage() {
        return new TasksPage().open();
    }

    @Step("Меню: перейти в «Новая задача»")
    public CreateTaskPage goToCreateForm(){
        return  new Header().openCreateForm();
    }

    @Step("Меню: перейти в «О проекте»")
    public AboutPage goToAbout() {
        return new Header().openAbout();
    }

    @Step("Меню: перейти во «Вход»")
    public LoginPage goToLogin() {
        return new Header().openLogin();
    }

    @Step("Меню: перейти в «Поиск задач»")
    public TasksPage goToSearch() {
        return new Header().openSearch();
    }

    @Step("Меню: нажать на логотип")
    public TasksPage goHomeByLogo() {
        return new Header().goHome();
    }

    @Step("Проверить, что открыта страница «{expectedHeading}» ({expectedPath})")
    public void checkPageOpened(BasePage page, String expectedHeading, String expectedPath) {
        page.heading().shouldHave(exactText(expectedHeading));
        webdriver().shouldHave(urlContaining(expectedPath));
    }

    @Step("Проверить, что в меню подсвечен пункт «{expectedItem}»")
    public void checkActiveMenuItem(String expectedItem) {
        new Header().activeLink().shouldHave(exactText(expectedItem));
    }

    @Step("Проверить, что в меню не подсвечен ни один пункт")
    public void checkNoActiveMenuItem() {
        new Header().activeLink().shouldNot(exist);
    }

    @Step("Открыть страницу создания задачи")
    public CreateTaskPage openCreateTaskPage(){
        return new CreateTaskPage().open();
    }





}
