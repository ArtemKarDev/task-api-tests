package ru.learning.taskapitests.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.steps.ui.NavigationUiSteps;
import ru.learning.taskapitests.steps.ui.TaskUiSteps;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;

import static com.codeborne.selenide.Selenide.closeWebDriver;

@ExtendWith(ScreenshotOnFailureExtension.class)
public class UiTestBase extends TestBase {

    public static final String ALLURE_LISTENER_NAME = "AllureSelenide";

    @Autowired
    protected TaskUiSteps uiStep;
    @Autowired
    protected NavigationUiSteps navStep;

    @BeforeEach
    protected void configureSelenide() {
        var browser = props.browser();

        Configuration.baseUrl = props.baseUrl();
        Configuration.browser = browser.name();
        Configuration.headless = browser.headless();
        Configuration.browserSize = browser.size();
        Configuration.timeout = browser.timeoutMs();

        // Скриншоты и исходник страницы при падении прикрепляет Allure-слушатель и расширение,
        // собственное сохранение Selenide отключаем, чтобы не плодить дубли файлов
        Configuration.screenshots = false;
        Configuration.savePageSource = false;

        String remoteUrl = browser.remoteUrl();
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            Configuration.remote = remoteUrl;
        }
    }

    @BeforeEach
    protected void addAllureListener() {
        SelenideLogger.addListener(ALLURE_LISTENER_NAME,
                new AllureSelenide()
                        .screenshots(false)
                        .savePageSource(false));
    }

    @AfterEach
    protected void closeBrowser() {
        SelenideLogger.removeListener(ALLURE_LISTENER_NAME);
        closeWebDriver();
    }

    protected CreateTaskPage submitInvalidCreateFormAndCheckNothingSaved(CreateTaskRequest request, int expectedErrors) {

        long before = dbStep.getTasksCount();

        CreateTaskPage page = uiStep.submitFormWithErrors(navStep.openCreateTaskPage(), request);

        navStep.checkPageOpened(page, "Создание задачи", "/tasks");
        uiStep.checkFormErrorsCount(page, expectedErrors);
        dbStep.checkTasksCount(before);
        return page;

    }
}
