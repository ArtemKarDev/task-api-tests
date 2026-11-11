package ru.learning.taskapitests.tests.ui;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.extensions.LoginAs;
import ru.learning.taskapitests.extensions.WithTask;
import ru.learning.taskapitests.generated.model.Task;
import ru.learning.taskapitests.ui.UiTestBase;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.Condition.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Epic("UI")
@Feature("Удаление задачи")
class UiDeleteTaskTests extends UiTestBase {

    @Test
    @WithTask
    @LoginAs(Role.ADMIN)
    @Tag(Tags.SMOKE)
    @DisplayName("Админ удаляет задачу: окно подтверждения, сообщение, строка исчезает")
    void adminShouldDeleteTask(Task task) {
        TasksPage page = navStep.openTasksPage();

        String dialogText = page.rowById(task.getId()).deleteAndConfirm();

        assertThat(dialogText).as("Текст окна подтверждения").isEqualTo("Удалить задачу?");
        page.flash().shouldHave(text("Задача удалена"));
        page.rowById(task.getId()).root().shouldNot(exist);
    }

    @Test
    @WithTask
    @LoginAs(Role.ADMIN)
    @DisplayName("Отмена в окне подтверждения: задача остаётся в списке")
    void shouldKeepTaskWhenDeleteCancelled(Task task) {
        TasksPage page = navStep.openTasksPage();

        page.rowById(task.getId()).deleteAndCancel();

        page.rowById(task.getId()).root().shouldBe(visible);
    }

    @Test
    @WithTask
    @DisplayName("Обычный пользователь не видит кнопку удаления")
    void userShouldNotSeeDeleteButton(Task task) {
        TasksPage page = navStep.openTasksPage();

        page.rowById(task.getId()).root().shouldBe(visible);
        page.rowById(task.getId()).deleteButton().shouldNot(exist);
    }
}