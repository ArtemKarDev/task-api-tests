package ru.learning.taskapitests.config;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Переходный слой: прежний набор методов, но значения теперь берёт Spring
 * (application.yml, профили, параметры -D, переменные окружения).
 * Будет удалён, когда тесты начнут получать TestProperties через внедрение (шаг 8-6).
 */
public final class TestConfig {

    // Порядок объявления важен: сначала контекст, потом то, что берётся из него
    private static final ConfigurableApplicationContext CONTEXT = start();
    private static final TestProperties PROPS = CONTEXT.getBean(TestProperties.class);
    private static final String ENV = CONTEXT.getEnvironment().getActiveProfiles()[0];

    private TestConfig() {
    }

    private static ConfigurableApplicationContext start() {
        SpringApplication application = new SpringApplication(SettingsConfig.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setBannerMode(Banner.Mode.OFF);
        return application.run();
    }

    public static String getEnv() {
        return ENV;
    }

    public static String getBaseUrl() {
        return required(PROPS.baseUrl(), "tests.base-url");
    }

    public static String getDbUrl() {
        return required(PROPS.db().url(), "tests.db.url");
    }

    public static String getDbUser() {
        return required(PROPS.db().user(), "tests.db.user");
    }

    public static String getDbPassword() {
        String password = PROPS.db().password();
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Не задан пароль БД для стенда '" + ENV
                    + "'. Задайте переменную окружения TESTS_DB_PASSWORD или параметр -Dtests.db.password=...");
        }
        return password;
    }

    public static String getBrowser() {
        return required(PROPS.browser().name(), "tests.browser.name");
    }

    public static boolean isHeadless() {
        return PROPS.browser().headless();
    }

    public static String getBrowserSize() {
        return required(PROPS.browser().size(), "tests.browser.size");
    }

    public static long getUiTimeoutMs() {
        return PROPS.browser().timeoutMs();
    }

    /** Адрес удалённого Selenium или null, если браузер запускается локально. */
    public static String getRemoteUrl() {
        String value = PROPS.browser().remoteUrl();
        return value == null || value.isBlank() ? null : value;
    }

    public static String getKafkaBootstrapServers() {
        return required(PROPS.kafka().bootstrapServers(), "tests.kafka.bootstrap-servers");
    }

    public static String getKafkaTopicIn() {
        return required(PROPS.kafka().topicIn(), "tests.kafka.topic-in");
    }

    public static String getKafkaTopicInDlq() {
        return required(PROPS.kafka().topicInDlq(), "tests.kafka.topic-in-dlq");
    }

    public static String getKafkaTopicOut() {
        return required(PROPS.kafka().topicOut(), "tests.kafka.topic-out");
    }

    /** Текстовое описание текущей конфигурации (без пароля) для логов и отчёта. */
    public static String describe() {
        String remote = getRemoteUrl();
        return """
                Стенд: %s
                base.url: %s
                db.url: %s
                db.user: %s
                kafka: %s
                browser: %s (headless=%s, size=%s)
                remote.url: %s
                """.formatted(
                ENV, getBaseUrl(), getDbUrl(), getDbUser(), getKafkaBootstrapServers(),
                getBrowser(), isHeadless(), getBrowserSize(),
                remote == null ? "(не задан, браузер запускается локально)" : remote);
    }

    private static String required(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задан параметр '" + key + "' для стенда '" + ENV + "'");
        }
        return value;
    }
}