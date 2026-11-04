package ru.learning.taskapitests.learning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.learning.taskapitests.Tags;

import static org.assertj.core.api.Assertions.assertThat;

/** Учебный эксперимент: как Spring собирает объекты. */
@Tag(Tags.UNIT)
@SpringJUnitConfig(SpringBasicsTests.Config.class)
class SpringBasicsTests {

    /** Обычный класс. Ничего «спринговского» внутри нет. */
    static class Greeter {
        private final String greeting;

        Greeter(String greeting) {
            this.greeting = greeting;
        }

        String greet(String name) {
            return greeting + ", " + name;
        }
    }

    /** Этому классу нужен Greeter. Сам он его не создаёт, а просит в конструкторе. */
    static class Welcome {
        private final Greeter greeter;

        Welcome(Greeter greeter) {
            this.greeter = greeter;
        }

        String forTester() {
            return greeter.greet("tester");
        }
    }

    /** Рецепт для Spring: какие объекты существуют и из чего собираются. */
    @Configuration
    static class Config {

        @Bean
        Greeter greeter() {
            return new Greeter("Hello");
        }

        @Bean
        Welcome welcome(Greeter greeter) {      // Spring сам подставит готовый greeter
            return new Welcome(greeter);
        }


    }

    @Autowired
    Welcome welcome;                            // Spring сам положит сюда готовый объект

    @Autowired
    Greeter greeter;

    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("Spring сам собрал цепочку объектов: Welcome получил Greeter")
    void shouldAssembleObjectsInChain() {
        assertThat(welcome.forTester()).isEqualTo("Hello, tester");
    }

    @Test
    @DisplayName("По умолчанию бин один на весь контекст: сколько ни проси, придёт тот же объект")
    void shouldGiveTheSameInstanceEveryTime() {
        assertThat(context.getBean(Greeter.class)).isSameAs(greeter);
        assertThat(context.getBean(Greeter.class)).isSameAs(context.getBean(Greeter.class));
    }

    @Test
    @DisplayName("Объекты внутри контекста связаны: Welcome использует именно этот Greeter")
    void shouldInjectTheSameGreeterEverywhere() {
        assertThat(context.getBean(Welcome.class)).isSameAs(welcome);
        assertThat(context.getBeansOfType(Greeter.class)).hasSize(1);
    }
}