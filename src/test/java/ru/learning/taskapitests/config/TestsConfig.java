package ru.learning.taskapitests.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Рецепт для всех тестов: настройки и классы-помощники (клиенты и шаги). */
@Configuration
@Import(SettingsConfig.class)
@ComponentScan(basePackages = {
        "ru.learning.taskapitests.client",
        "ru.learning.taskapitests.db",
        "ru.learning.taskapitests.specs",
        "ru.learning.taskapitests.steps"
})
public class TestsConfig {
}