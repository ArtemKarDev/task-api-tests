package ru.learning.taskapitests.tests.unit;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.config.SettingsConfig;
import ru.learning.taskapitests.config.TestProperties;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.UNIT)
@Epic("Конфигурация")
@Feature("Профили стендов")
@SpringBootTest(classes = SettingsConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TestConfigTests {

    @Autowired
    TestProperties props;

    @Autowired
    Environment environment;

    @Test
    @DisplayName("Активный стенд определён, адреса приложения и БД заданы")
    void shouldResolveActiveEnvironment() {
        String env = environment.getActiveProfiles()[0];
        Allure.addAttachment("Конфигурация стенда", "text/plain", describe(env));

        assertThat(env).as("Активный стенд").isIn("local", "dev", "stage");
        assertThat(props.baseUrl()).as("Адрес приложения").startsWith("http://");
        assertThat(props.db().url()).as("Адрес БД").startsWith("jdbc:postgresql://");
    }

    /** Текстовое описание текущей конфигурации (без пароля) для отчёта. */
    private String describe(String env) {
        String remote = props.browser().remoteUrl();
        return """
                Стенд: %s
                base-url: %s
                db.url: %s
                db.user: %s
                kafka: %s
                browser: %s (headless=%s, size=%s)
                remote-url: %s
                """.formatted(
                env, props.baseUrl(), props.db().url(), props.db().user(), props.kafka().bootstrapServers(),
                props.browser().name(), props.browser().headless(), props.browser().size(),
                remote == null || remote.isBlank() ? "(не задан, браузер запускается локально)" : remote);
    }
}