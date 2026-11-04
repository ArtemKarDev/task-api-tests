package ru.learning.taskapitests.learning.scan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.learning.taskapitests.Tags;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.UNIT)
@SpringJUnitConfig(ScanConfig.class)
class SpringScanTests {

    @Autowired
    ApplicationContext context;

    @Autowired
    Welcome welcome;

    @Test
    @DisplayName("Spring нашёл помеченные классы сам и подставил зависимости через конструктор")
    void shouldFindComponentsByScanning() {
        assertThat(context.getBeanNamesForType(Greeter.class)).hasSize(1);
        assertThat(welcome.greet("tester")).isEqualTo("Hello, tester");
    }

    @Test
    @DisplayName("singleton: Greeter один на весь контекст")
    void shouldShareSingleton() {
        assertThat(context.getBean(Greeter.class)).isSameAs(context.getBean(Greeter.class));
    }

    @Test
    @DisplayName("prototype: каждая просьба к контексту даёт новый Counter")
    void shouldCreateNewPrototypeOnEveryRequest() {
        Counter first = context.getBean(Counter.class);
        Counter second = context.getBean(Counter.class);

        assertThat(first).isNotSameAs(second);
    }

    @Test
    @DisplayName("Ловушка: prototype, внедрённый в singleton, создаётся один раз и больше не меняется")
    void shouldFreezePrototypeInsideSingleton() {
        assertThat(welcome.frozenCounter()).isSameAs(welcome.frozenCounter());
    }

    @Test
    @DisplayName("Выход из ловушки: ObjectProvider выдаёт новый Counter при каждом вызове")
    void shouldGiveFreshPrototypeThroughProvider() {
        Counter first = welcome.freshCounter();
        Counter second = welcome.freshCounter();

        assertThat(first).isNotSameAs(second);
        assertThat(first.next()).isEqualTo(1);
        assertThat(second.next()).isEqualTo(1);
    }
}