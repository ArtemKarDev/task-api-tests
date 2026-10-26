package ru.learning.taskapitests.tests.ui;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.ui.UiTestBase;
import ru.learning.taskapitests.ui.pages.AboutPage;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;
import ru.learning.taskapitests.ui.pages.LoginPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Epic("UI")
@Feature("Навигация")
class NavigationUiTests extends UiTestBase {

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Меню шапки ведёт на все разделы, текущий пункт подсвечивается")
    void shouldNavigateThroughHeaderMenu() {
        navStep.openTasksPage();

        CreateTaskPage createPage = navStep.goToCreateForm();
        navStep.checkPageOpened(createPage, "Создание задачи", "/tasks/new");
        navStep.checkActiveMenuItem("Новая задача");

        AboutPage aboutPage = navStep.goToAbout();
        navStep.checkPageOpened(aboutPage, "О проекте", "/about");
        navStep.checkActiveMenuItem("О проекте");

        TasksPage tasksPage = navStep.goToSearch();
        navStep.checkPageOpened(tasksPage, "Поиск задач", "/tasks");
        navStep.checkActiveMenuItem("Поиск задач");
    }

    @Test
    @DisplayName("Страница входа открывается, пункт меню не подсвечивается")
    void shouldOpenLoginPageWithoutActiveMenuItem() {
        navStep.openTasksPage();

        LoginPage loginPage = navStep.goToLogin();

        navStep.checkPageOpened(loginPage, "Вход", "/login");
        navStep.checkNoActiveMenuItem();
    }

    @Test
    @DisplayName("Клик по логотипу возвращает на список задач")
    void shouldReturnToTasksPageByLogo() {
        navStep.openTasksPage();
        navStep.goToAbout();

        TasksPage page = navStep.goHomeByLogo();

        navStep.checkPageOpened(page, "Поиск задач", "/tasks");
    }
}