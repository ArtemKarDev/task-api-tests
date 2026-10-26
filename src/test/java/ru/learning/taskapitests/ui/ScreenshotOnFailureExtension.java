package ru.learning.taskapitests.ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;


public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotOnFailureExtension.class);

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        if (!WebDriverRunner.hasWebDriverStarted()) {
            log.warn("Тест упал, но браузер не был запущен: вложения не созданы");
            return;
        }
        safely("адрес страницы", this::attachUrl);
        safely("скриншот", this::attachScreenshot);
        safely("исходный код страницы", this::attachPageSource);
    }

    private void attachUrl() {
        Allure.addAttachment("Адрес страницы при падении", "text/plain", WebDriverRunner.url());
    }

    private void attachScreenshot() {
        byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
        if (screenshot != null) {
            Allure.addAttachment("Скриншот при падении", "image/png",
                    new ByteArrayInputStream(screenshot), "png");
        }
    }

    private void attachPageSource() {
        Allure.addAttachment("Исходный код страницы", "text/html", WebDriverRunner.source(), "html");
    }


    private void safely(String what, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            log.warn("Не удалось приложить к отчёту {}: {}", what, e.getMessage());
        }
    }
}