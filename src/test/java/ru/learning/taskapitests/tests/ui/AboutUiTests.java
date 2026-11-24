package ru.learning.taskapitests.tests.ui;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.extensions.LoginAs;
import ru.learning.taskapitests.ui.BrowserTestBase;
import ru.learning.taskapitests.ui.pages.AboutPage;
import ru.learning.taskapitests.ui.pages.ProfilePasswordPage;

import java.util.List;


@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Epic("UI")
@Feature("Страница о проекте")
public class AboutUiTests extends BrowserTestBase {

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Гость открывает / и попадает на «О проекте»")
    void shouldOpenAboutPage() {
        AboutPage page = navStep.openMainPageAsGuest();
        navStep.checkPageOpened(page, "Артём Карлашов", AboutPage.PATH);
    }

    @Test
    @DisplayName("У гостя в шапке есть «Войти» и нет ссылок на задачи")
    void guestHeaderShowsLoginAndHidesTaskLinks() {
        AboutPage page = navStep.openAboutPage();
        navStep.checkGuestHeader(page.header());
        navStep.checkActiveMenuItem("О проекте");

    }

    @Test
    @LoginAs(Role.USER)
    @DisplayName("У вошедшего пользователя в шапке есть ссылки на задачи")
    void userHeaderShowsTaskLinks() {
        AboutPage page = navStep.openAboutPage();
        navStep.checkUserHeader(page.header());
    }

    @Test
    @DisplayName("на странице есть разделы и ссылки на оба репозитория и Swagger UI")
    void shouldVisibleBlocksRepsAndSwaggerUi() {
        List<String> blocks = List.of("author", "path", "project", "now", "diagrams", "try", "plans", "source");

        AboutPage page = navStep.openAboutPage();
        for (String block : blocks) {
            aboutUiSteps.checkSectionVisible(page, block);
        }
        aboutUiSteps.checkRepoLinks(page);
        aboutUiSteps.checkSwaggerLink(page);
    }


    @Test
    @DisplayName("Гость входит под учебным пользователем кнопкой на странице «О проекте»")
    void guestLogsInWithDemoButton() {
        AboutPage page = navStep.openAboutPage();
        TestProperties.Account demo = aboutUiSteps.readDemoAccount(page);

        aboutUiSteps.loginAsGuest(page);

        aboutUiSteps.checkLoggedInAs(demo.username());
    }

    @Test
    @DisplayName("Учебный пользователь не может сменить пароль на странице профиля")
    void demoCannotChangePasswordInProfile() {
        AboutPage aboutPage = navStep.openAboutPage();
        TestProperties.Account demo = aboutUiSteps.readDemoAccount(aboutPage);
        aboutUiSteps.loginAsGuest(aboutPage);

        ProfilePasswordPage profile = new ProfilePasswordPage().open()
                .changePassword(demo.password(), "Another-pass-1234");

        aboutUiSteps.checkDemoPasswordChangeRejected(profile);
    }

}
