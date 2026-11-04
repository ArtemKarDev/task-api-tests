package ru.learning.taskapitests.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(TestProperties.class)
public class SettingsConfig {

    @Bean
    public ProfileGuard profileGuard(Environment environment){
        return new ProfileGuard(environment);
    }
}
