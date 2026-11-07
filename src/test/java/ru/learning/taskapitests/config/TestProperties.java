package ru.learning.taskapitests.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки тестов. Значения Spring берёт из application.yml, переменных окружения и -D.
 */
@ConfigurationProperties(prefix = "tests")
public record TestProperties(String baseUrl, Db db, Browser browser, Kafka kafka, Auth auth) {

    public record Db(String url, String user, String password) {
    }

    public record Browser(String name, boolean headless, String size, long timeoutMs, String remoteUrl) {
    }

    public record Kafka(String bootstrapServers, String topicIn, String topicInDlq, String topicOut) {
    }

    public record Auth(Account user, Account admin) {
    }

    public record Account(String username, String password) {
    }

}