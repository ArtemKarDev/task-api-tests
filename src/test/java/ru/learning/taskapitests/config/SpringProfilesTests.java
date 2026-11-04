package ru.learning.taskapitests.config;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.learning.taskapitests.Tags;

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
        void shouldUseLocalValues(){
            assertThat(props.baseUrl()).isEqualTo("http://localhost:8080");
            assertThat(props.db().url()).isEqualTo("jdbc:postgresql://localhost:5433/taskdb");
            assertThat(props.db().user()).isEqualTo("taskuser");
            assertThat(props.db().password()).isEqualTo("local_dev_password");
            assertThat(props.kafka().bootstrapServers()).isEqualTo("localhost:9092");
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
    }
}
