package ru.learning.taskapitests.ui;

import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;
import ru.learning.taskapitests.extensions.LoginAs;

@LoginAs
public class UiTestBase extends BrowserTestBase {

    protected CreateTaskPage submitInvalidCreateForm(CreateTaskRequest request, int expectedErrors) {

        CreateTaskPage page = uiStep.submitFormWithErrors(navStep.openCreateTaskPage(), request);

        navStep.checkPageOpened(page, "Создание задачи", "/tasks");
        uiStep.checkFormErrorsCount(page, expectedErrors);

        return page;
    }
}