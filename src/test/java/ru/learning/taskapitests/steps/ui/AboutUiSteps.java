package ru.learning.taskapitests.steps.ui;


import io.qameta.allure.Step;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.ui.components.Header;
import ru.learning.taskapitests.ui.pages.AboutPage;
import ru.learning.taskapitests.ui.pages.ProfilePasswordPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.page;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static com.codeborne.selenide.Selenide.webdriver;

@Component
public class AboutUiSteps {

    private static final String GITHUB = "https://github.com/ArtemKarDev/";

    @Step("Проверить, что на странице есть раздел «{sectionId}»")
    public void checkSectionVisible(AboutPage page, String sectionId) {
        page.section(sectionId).shouldBe(visible);
    }

    @Step("Проверить ссылки на репозитории сервиса и автотестов")
    public void checkRepoLinks(AboutPage page) {
        page.repoLinks().shouldHave(size(2));
        page.repoLinks().get(0).shouldHave(attribute("href", GITHUB + "task-api-service"));
        page.repoLinks().get(1).shouldHave(attribute("href", GITHUB + "task-api-tests"));
    }

    @Step("Проверить ссылку на Swagger UI")
    public void checkSwaggerLink(AboutPage page) {
        page.swaggerLink().shouldBe(visible);
    }


    @Step("прочитать на странице чебные логин и пароль")
    public TestProperties.Account readDemoAccount(AboutPage page) {
        assumeTrue(page.demoBlock().exists(), "Учебный вход на стенде выключен");
        return new TestProperties.Account(page.demoUsername().getText(), page.demoPassword().getText());
    }

    @Step("Войти как гость кнопкой на странице «О проекте»")
    public TasksPage loginAsGuest(AboutPage page) {
        return page.loginAsGuest();
    }

    @Step("Проверить, что вошёл пользователь «{username}»")
    public void checkLoggedInAs(String username) {
        webdriver().shouldHave(urlContaining("/tasks"));
        new Header().userName().shouldHave(exactText(username));
    }

    @Step("Проверить, что смена пароля отклонена для учебного пользователя")
    public void checkDemoPasswordChangeRejected(ProfilePasswordPage page) {
        webdriver().shouldHave(urlContaining(ProfilePasswordPage.PATH));
        page.formError().shouldHave(text("нельзя менять пароль"));
    }


}
