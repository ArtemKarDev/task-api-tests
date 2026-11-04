package ru.learning.taskapitests.learning.scan;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Без @Bean: ищем помеченные классы в этом пакете и ниже. */
@Configuration
@ComponentScan
public class ScanConfig {
}