package ru.learning.taskapitests.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SettingsConfig.class)
@ComponentScan(basePackages = {
        "ru.learning.taskapitests.client",
        "ru.learning.taskapitests.steps"
})
public class TestsConfig {
}
