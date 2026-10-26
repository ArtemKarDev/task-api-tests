package ru.learning.taskapitests.tests.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.ui.UiTestBase;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.Condition.exactText;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("demo")
class FailureDemoTest extends UiTestBase {

    @Test
    @DisplayName("ДЕМО: падает проверка Selenide")
    void selenideAssertionFails() {
        TasksPage page = navStep.openTasksPage();

        page.heading().shouldHave(exactText("Неверный заголовок"));
    }

    @Test
    @DisplayName("ДЕМО: падает обычный assertThat")
    void assertJAssertionFails() {
        TasksPage page = navStep.openTasksPage();

        assertThat(page.heading().getText()).isEqualTo("Неверный заголовок");
    }
}