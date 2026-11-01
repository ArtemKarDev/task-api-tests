package ru.learning.taskapitests.tests.kafka;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.config.TestConfig;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.KAFKA)
@Tag(Tags.REGRESSION)
@Epic("Kafka")
@Feature("Подключение")
class KafkaConnectionTests {

    @Test
    @DisplayName("Брокер доступен, три рабочих топика созданы приложением")
    void shouldSeeAllWorkingTopics() throws Exception {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, TestConfig.getKafkaBootstrapServers());
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");

        try (AdminClient admin = AdminClient.create(properties)) {
            Set<String> topics = admin.listTopics().names().get(15, TimeUnit.SECONDS);

            assertThat(topics)
                    .as("Топики на брокере " + TestConfig.getKafkaBootstrapServers())
                    .contains(TestConfig.getKafkaTopicIn(),
                            TestConfig.getKafkaTopicInDlq(),
                            TestConfig.getKafkaTopicOut());
        }
    }
}