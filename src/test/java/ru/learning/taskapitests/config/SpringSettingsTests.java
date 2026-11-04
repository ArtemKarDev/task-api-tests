package ru.learning.taskapitests.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;                                                                    // НОВОЕ
import org.slf4j.LoggerFactory;                                                             // НОВОЕ
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.source.ConfigurationProperty;           // НОВОЕ
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;       // НОВОЕ
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;     // НОВОЕ
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;    // НОВОЕ
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;                                            // НОВОЕ
import ru.learning.taskapitests.Tags;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.UNIT)
@SpringBootTest(classes = SettingsConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SpringSettingsTests {

    private static final Logger log = LoggerFactory.getLogger(SpringSettingsTests.class);   // НОВОЕ
    private static final String NOT_SET = "(нигде не задано)";                              // НОВОЕ

    @Autowired
    TestProperties props;

    @Autowired                                                                              // НОВОЕ
    Environment environment;

    @Test
    @DisplayName("Значения из application.yml разложены по полям Java-класса")
    void shouldBindYamlToRecord() {
        assertThat(props.baseUrl()).isNotBlank();
        assertThat(props.db().url()).startsWith("jdbc:postgresql://");
        assertThat(props.db().user()).isEqualTo("taskuser");
        assertThat(props.browser().name()).isEqualTo("chrome");
        assertThat(props.browser().timeoutMs()).isEqualTo(6000);
        assertThat(props.browser().remoteUrl()).isNullOrEmpty();
        assertThat(props.kafka().topicOut()).isEqualTo("task_out");
    }

    @Test                                                                                   // НОВОЕ
    @DisplayName("Детектор: откуда пришло значение (смотрите строки в журнале теста)")
    void shouldTellWhereValueCameFrom() {
        log.info("tests.base-url = {}", describe("tests.base-url"));
        log.info("tests.kafka.bootstrap-servers = {}", describe("tests.kafka.bootstrap-servers"));

        assertThat(describe("tests.base-url")).isNotEqualTo(NOT_SET);
    }

    /** Идёт по источникам настроек сверху вниз и показывает, какой первым дал значение и откуда именно. */
    private String describe(String key) {                                                   // НОВОЕ
        ConfigurationPropertyName name = ConfigurationPropertyName.of(key);
        for (ConfigurationPropertySource source : ConfigurationPropertySources.get(environment)) {
            ConfigurationProperty property = source.getConfigurationProperty(name);
            if (property != null) {
                return property.getValue() + "   <=   " + property.getOrigin();
            }
        }
        return NOT_SET;
    }
}