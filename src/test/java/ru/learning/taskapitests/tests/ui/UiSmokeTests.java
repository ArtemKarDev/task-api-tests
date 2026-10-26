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
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.Condition.visible;

@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Epic("UI")
@Feature("Список задач")
public class UiSmokeTests extends UiTestBase {

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Страница списка задач открывается")
    void shouldOpenTasksPage(){
        TasksPage page = navStep.openTasksPage();

        navStep.checkPageOpened(page, "Поиск задач", "/tasks");
        navStep.checkActiveMenuItem("Поиск задач");
        page.searchInput().shouldBe(visible);
        page.statusFilter().shouldBe(visible);
    }

    @Test
    @Tag(Tags.SMOKE)
    @DisplayName("Задача, созданная через API, видна в списке с верными данными")
    void shouldShowTaskCreatedViaApi() {
        var request = TaskDataFactory.defaultTaskRequestDTO();
        Response response = step.createTaskDto(request);
        step.checkStatus(response, 201);
        long taskId = response.jsonPath().getLong("id");

        TasksPage page = navStep.openTasksPage();

        uiStep.checkTaskInList(page, taskId, request);
    }

}
