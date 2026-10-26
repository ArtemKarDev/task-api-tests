package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.config.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.UNIT)
@Epic("Конфигурация")
@Feature("Профили стендов")
@DisplayName("Тест конфигурации стенда")
class TestConfigTests {

    @Test
    @DisplayName("Активный стенд определён, адреса приложения и БД заданы")
    void shouldResolveActiveEnvironment() {
        Allure.addAttachment("Конфигурация стенда", "text/plain", TestConfig.describe());

        assertThat(TestConfig.getEnv()).as("Активный стенд").isIn("local", "dev", "stage");
        assertThat(TestConfig.getBaseUrl()).as("Адрес приложения").startsWith("http://");
        assertThat(TestConfig.getDbUrl()).as("Адрес БД").startsWith("jdbc:postgresql://");
    }
}
