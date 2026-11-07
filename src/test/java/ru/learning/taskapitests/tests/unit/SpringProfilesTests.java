package ru.learning.taskapitests.tests.unit;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.config.SettingsConfig;
import ru.learning.taskapitests.config.TestProperties;

import static org.assertj.core.api.Assertions.assertThat;


@Tag(Tags.UNIT)
public class SpringProfilesTests {

    @Nested
    @SpringBootTest(classes = SettingsConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @ActiveProfiles("local")
    @DisplayName("Профиль local")
    class Local {

        @Autowired
        TestProperties props;

        @Test
        @DisplayName("Значения стенда local, пароль берётся из файла профиля")
        void shouldUseLocalValues() {
            assertThat(props.baseUrl()).isEqualTo("http://localhost:8080");
            assertThat(props.db().url()).isEqualTo("jdbc:postgresql://localhost:5433/taskdb");
            assertThat(props.db().user()).isEqualTo("taskuser");
            assertThat(props.db().password()).isEqualTo("local_dev_password");
            assertThat(props.kafka().bootstrapServers()).isEqualTo("localhost:9092");
        }

        @Test
        @DisplayName("Значения пользователей стенда local, пароль берётся из файла профиля")
        void shouldUseLocalValuesUserCredentials() {
            assertThat(props.auth())
                    .as("ветка tests.auth не попала в настройки").isNotNull();
            assertThat(props.auth().user().username()).isEqualTo("user");
            assertThat(props.auth().user().password())
                    .as("Пароль user для local (application-local.yml)").isEqualTo("local_user_password");
            assertThat(props.auth().admin().username()).isEqualTo("admin");
            assertThat(props.auth().admin().password())
                    .as("Пароль admin для local (application-local.yml)").isEqualTo("local_admin_password");
        }
    }

    @Nested
    @SpringBootTest(classes = SettingsConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @ActiveProfiles("dev")
    @DisplayName("Профиль dev")
    class Dev {

        @Autowired
        TestProperties props;

        @Test
        @DisplayName("Значения стенда dev, пароль приходит только из переменной окружения")
        void shouldUseDevValues() {
            assertThat(props.baseUrl()).isEqualTo("http://localhost:8180");
            assertThat(props.db().url()).isEqualTo("jdbc:postgresql://localhost:5432/taskdb");
            assertThat(props.db().user()).isEqualTo("taskuser");
            assertThat(props.db().password())
                    .as("Пароль dev не хранится в файлах: он приходит из TESTS_DB_PASSWORD")
                    .isEqualTo(System.getenv("TESTS_DB_PASSWORD"));
            assertThat(props.kafka().bootstrapServers()).isEqualTo("localhost:9192");
        }

        @Test
        @DisplayName("Значения пользователей стенда dev, пароль берётся из переменной окружения")
        void shouldUseDevValuesUserCredentials() {
            assertThat(props.auth()).isNotNull();
            assertThat(props.auth().user().username()).isEqualTo("user");
            assertThat(props.auth().user().password())
                    .as("Пароль dev не хранится в файлах: он приходит из TESTS_AUTH_USER_PASSWORD")
                    .isEqualTo(System.getenv("TESTS_AUTH_USER_PASSWORD"));
            assertThat(props.auth().admin().username()).isEqualTo("admin");
            assertThat(props.auth().admin().password())
                    .as("Пароль dev не хранится в файлах: он приходит из TESTS_AUTH_ADMIN_PASSWORD")
                    .isEqualTo(System.getenv("TESTS_AUTH_ADMIN_PASSWORD"));
        }

    }

    @Nested
    @SpringBootTest(classes = SettingsConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @ActiveProfiles("stage")
    @DisplayName("Профиль stage")
    class Stage {

        @Autowired
        TestProperties props;

        @Test
        @DisplayName("Значения стенда stage, пароль приходит только из переменной окружения")
        void shouldUseStageValues() {
            assertThat(props.baseUrl()).isEqualTo("http://localhost:8280");
            assertThat(props.db().url()).isEqualTo("jdbc:postgresql://localhost:5434/taskdb");
            assertThat(props.db().user()).isEqualTo("taskuser");
            assertThat(props.db().password())
                    .as("Пароль stage не хранится в файлах: он приходит из TESTS_DB_PASSWORD")
                    .isEqualTo(System.getenv("TESTS_DB_PASSWORD"));
            assertThat(props.kafka().bootstrapServers()).isEqualTo("localhost:9292");
        }

        @Test
        @DisplayName("Значения пользователей стенда stage, пароль берётся из переменной окружения")
        void shouldUseStageValuesUserCredentials() {
            assertThat(props.auth()).isNotNull();
            assertThat(props.auth().user().username()).isEqualTo("user");
            assertThat(props.auth().user().password())
                    .as("Пароль stage не хранится в файлах: он приходит из TESTS_AUTH_USER_PASSWORD")
                    .isEqualTo(System.getenv("TESTS_AUTH_USER_PASSWORD"));
            assertThat(props.auth().admin().username()).isEqualTo("admin");
            assertThat(props.auth().admin().password())
                    .as("Пароль stage не хранится в файлах: он приходит из TESTS_AUTH_ADMIN_PASSWORD")
                    .isEqualTo(System.getenv("TESTS_AUTH_ADMIN_PASSWORD"));
        }
    }
}
