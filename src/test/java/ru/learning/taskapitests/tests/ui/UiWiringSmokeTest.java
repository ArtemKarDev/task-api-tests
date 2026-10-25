package ru.learning.taskapitests.tests.ui;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.ui.UiTestBase;
import ru.learning.taskapitests.ui.components.TaskRow;
import ru.learning.taskapitests.ui.pages.AboutPage;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;
import ru.learning.taskapitests.ui.pages.LoginPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

import java.util.UUID;

import static com.codeborne.selenide.Condition.*;
import static org.assertj.core.api.Assertions.assertThat;

// ВРЕМЕННЫЙ тест: проверка каркаса. В части 1c его заменят тесты на Page Object.
@Tag(Tags.UI)
@Tag(Tags.SMOKE)
@Epic("UI")
@Feature("Каркас UI-тестов")
class UiWiringSmokeTest extends UiTestBase {

    @Test
    @DisplayName("Страница списка задач открывается в браузере")
    void shouldOpenTasksPage() {
        TasksPage tasksPage = new TasksPage().open();

        tasksPage.heading().shouldHave(text("Поиск задач"));
        tasksPage.header().activeLink().shouldHave(text("Поиск задач"));
    }

    @Test
    @DisplayName("Page Object: строка задачи, навигация и удаление через UI")
    void shouldWorkThroughPageObjects() {
        String title = "UI-проверка " + UUID.randomUUID();
        Response created = step.createTaskDto(TaskDataFactory.customTaskRequestDTO(r -> r.title(title)));
        step.checkStatus(created, 201);
        long id = created.jsonPath().getLong("id");

        TasksPage tasksPage = new TasksPage().open();
        TaskRow row = tasksPage.rowById(id);
        row.title().shouldHave(exactText(title));
        row.description().shouldHave(exactText("Создана через REST Assured"));
        row.assignee().shouldHave(exactText("tester"));
        row.priority().shouldHave(exactText("MEDIUM"));
        row.status().shouldHave(exactText("NEW"));
        row.editButton().shouldBe(visible);
        row.deleteButton().shouldBe(visible);

        AboutPage aboutPage = tasksPage.header().openAbout();
        aboutPage.heading().shouldHave(text("О проекте"));
        aboutPage.content().shouldBe(visible);
        aboutPage.header().activeLink().shouldHave(text("О проекте"));

        LoginPage loginPage = aboutPage.header().openLogin();
        loginPage.usernameInput().shouldBe(visible);
        loginPage.passwordInput().shouldBe(visible);

        tasksPage = loginPage.header().openSearch();

        String dialogText = tasksPage.rowByTitle(title).deleteAndConfirm();

        assertThat(dialogText).as("Текст окна подтверждения").isEqualTo("Удалить задачу?");
        tasksPage.flash().shouldHave(text("Задача удалена"));
        tasksPage.rowById(id).root().shouldNot(exist);

    }

    @Test
    @DisplayName("Page Object: создание задачи через форму")
    void shouldCreateTaskThroughForm() {
        String title = "Форма " + UUID.randomUUID();

        CreateTaskPage createPage = new TasksPage().open().header().openCreateForm();
        createPage.form()
                .fillTitle(title)
                .fillAssignee("tester")
                .selectPriority("HIGH")
                .selectStatus("NEW");
        TasksPage tasksPage = createPage.submitValid();

        tasksPage.flash().shouldHave(text("Задача создана"));
        TaskRow row = tasksPage.rowByTitle(title);
        row.priority().shouldHave(exactText("HIGH"));
        row.status().shouldHave(exactText("NEW"));

        row.deleteAndConfirm();
    }

    @Test
    @DisplayName("Пустая форма создания показывает ошибки у обязательных полей")
    void shouldShowErrorsForEmptyCreateForm() {
        CreateTaskPage page = new CreateTaskPage().open();

        page.submitInvalid();

        page.form().titleError().shouldHave(exactText("Введите название задачи"));
        page.form().assigneeError().shouldHave(exactText("Укажите исполнителя"));
        page.form().priorityError().shouldHave(exactText("Выберите приоритет"));
        page.form().statusError().shouldHave(exactText("Выберите статус"));
    }
}