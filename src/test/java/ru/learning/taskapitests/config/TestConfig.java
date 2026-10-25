package ru.learning.taskapitests.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;


public class TestConfig {

    private static final List<String> ALLOWED_ENVS = List.of("local", "dev", "stage");

    private static final String LOCAL_DEV_DB_PASSWORD = "local_dev_password";

    private static final String ENV = resolveEnv();
    private static final Properties FILE_PROPERTIES = loadProperties();

    private TestConfig() {
    }

    public static String getEnv() {
        return ENV;
    }

    public static String getBaseUrl() {
        return require("base.url");
    }

    public static String getDbUrl() {
        return require("db.url");
    }

    public static String getDbUser() {
        return require("db.user");
    }

    public static String getDbPassword() {
        String password = get("db.password");
        if (!isBlank(password)) {
            return password;
        }
        if ("local".equals(ENV)) {
            return LOCAL_DEV_DB_PASSWORD;
        }
        throw new IllegalStateException("Не задан пароль БД для стенда" + ENV
                + ". Задайте переменную окружения DB_PASSWORD или параметр -Db.password=...");
    }

    public static String getBrowser() {
        return require("browser");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(require("headless"));
    }

    public static String getBrowserSize() {
        return require("browser.size");
    }

    public static long getUiTimeoutMs() {
        return Long.parseLong(require("ui.timeout.ms"));
    }

    public static String getRemoteUrl() {
        String value = get("remote.url");
        return isBlank(value) ? null : value;
    }

    /**
     * Текстовое описание текущей конфигурации (без пароля) для логов и отчёта.
     */
    public static String describe() {
        String remote = getRemoteUrl();
        return """
                Стенд: %s
                base.url: %s
                db.url: %s
                db.user: %s
                browser: %s (headless=%s, size=%s)
                remote.url: %s
                """.formatted(
                ENV, getBaseUrl(), getDbUrl(), getDbUser(),
                getBrowser(), isHeadless(), getBrowserSize(),
                remote == null ? "(не задан, браузер запускается локально)" : remote);
    }

    private static String get(String key) {
        String fromSystem = System.getProperty(key);
        if (!isBlank(fromSystem)) {
            return fromSystem;
        }
        String fromEnvironment = System.getenv(toEnvName(key));
        if (!isBlank(fromEnvironment)) {
            return fromEnvironment;
        }
        return FILE_PROPERTIES.getProperty(key);
    }

    private static String require(String key) {
        String value = get(key);
        if (isBlank(value)) {
            throw new IllegalStateException("Не задан параметр '" + key + "' для стенда '" + ENV
                    + "'. Добавьте его в config/" + ENV + ".properties, "
                    + "или задайте -D" + key + " / переменную " + toEnvName(key));
        }
        return value;
    }

    private static String resolveEnv() {
        String value = System.getProperty("env");
        if (isBlank(value)) {
            value = System.getenv("TEST_ENV");
        }
        if (isBlank(value)) {
            value = "local";
        }
        value = value.trim().toLowerCase();
        if (!ALLOWED_ENVS.contains(value)) {
            throw new IllegalStateException("Неизвестный стенд '" + value + "'. Допустимо: " + ALLOWED_ENVS);
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        load(properties, "config/default.properties");
        load(properties, "config/" + ENV + ".properties");
        return properties;
    }

    private static void load(Properties target, String resourcePath) {
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Файл " + resourcePath + " не найден в src/test/resources");
            }
            target.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + resourcePath, e);
        }
    }

    private static String toEnvName(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
