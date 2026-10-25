package ru.learning.taskapitests.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.config.TestConfig;

import static com.codeborne.selenide.Selenide.closeWebDriver;

public class UiTestBase extends TestBase {

    public static final String ALLURE_LISTENER_NAME = "AllureSelenide";

    @BeforeAll
    public static void configureSelenide(){
        Configuration.baseUrl = TestConfig.getBaseUrl();
        Configuration.browser = TestConfig.getBrowser();
        Configuration.headless = TestConfig.isHeadless();
        Configuration.browserSize = TestConfig.getBrowserSize();
        Configuration.timeout = TestConfig.getUiTimeoutMs();

        Configuration.screenshots = false;
        Configuration.savePageSource = false;

        String remoteUrl = TestConfig.getRemoteUrl();
        if (remoteUrl != null) {
            Configuration.remote = remoteUrl;
        }
    }

    @BeforeEach
    protected void addAllureListener(){
        SelenideLogger.addListener(ALLURE_LISTENER_NAME,
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(true));
    }

    @AfterEach
    protected void closeBrowser(){
        SelenideLogger.removeListener(ALLURE_LISTENER_NAME);
        closeWebDriver();
    }

}
