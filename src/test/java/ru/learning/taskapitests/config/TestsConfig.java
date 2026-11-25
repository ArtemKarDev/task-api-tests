package ru.learning.taskapitests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.learning.taskapitests.utils.ObjectMappers;

/** Рецепт для всех тестов: настройки и классы-помощники (клиенты и шаги). */
@Configuration
@Import(SettingsConfig.class)
@ComponentScan(basePackages = {
        "ru.learning.taskapitests.auth",
        "ru.learning.taskapitests.client",
        "ru.learning.taskapitests.db",
        "ru.learning.taskapitests.specs",
        "ru.learning.taskapitests.steps"
})
public class TestsConfig {

    @Bean
    ObjectMapper objectMapper() {
        return ObjectMappers.create();
    }

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(TestProperties props) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("test-db");
        config.setJdbcUrl(props.db().url());
        config.setUsername(props.db().user());
        config.setPassword(props.db().password());
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(5_000);
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }


}