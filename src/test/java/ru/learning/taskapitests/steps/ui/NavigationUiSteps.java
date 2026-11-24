package ru.learning.taskapitests.steps.ui;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.ui.components.Header;
import ru.learning.taskapitests.ui.pages.*;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

@Component
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

    @Step("Проверить, что в меню нет пунктов с задачами и есть Вход")
    public void checkGuestHeader(Header header){
        header.loginLink().shouldBe(visible);
        header.searchLink().shouldNot(exist);
        header.createLink().shouldNot(exist);
    }


    @Step("Проверить, что в меню не подсвечен ни один пункт")
    public void checkNoActiveMenuItem() {
        new Header().activeLink().shouldNot(exist);
    }

    @Step("Открыть страницу создания задачи")
    public CreateTaskPage openCreateTaskPage(){
        return new CreateTaskPage().open();
    }

    @Step("Открыть страницу о Проекте")
    public AboutPage openAboutPage(){
         Selenide.open(AboutPage.PATH);
        return new AboutPage();
    }

    @Step("Проверить шапку вошедшего пользователя: есть ссылки на задачи")
    public void checkUserHeader(Header header) {
        header.searchLink().shouldBe(visible);
        header.createLink().shouldBe(visible);
    }

    public AboutPage openMainPageAsGuest() {
        Selenide.open("/");
        return new AboutPage();
    }
}
