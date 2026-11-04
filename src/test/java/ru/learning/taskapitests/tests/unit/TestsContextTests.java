package ru.learning.taskapitests.tests.unit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.config.TestsConfig;
import ru.learning.taskapitests.steps.CreatedTasksRegistry;
import ru.learning.taskapitests.steps.DbSteps;
import ru.learning.taskapitests.steps.KafkaSteps;
import ru.learning.taskapitests.steps.TaskSteps;
import ru.learning.taskapitests.steps.ui.NavigationUiSteps;
import ru.learning.taskapitests.steps.ui.TaskUiSteps;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.UNIT)
@SpringBootTest(classes = TestsConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TestsContextTests {

    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("Spring создал все клиенты, шаги, настройки и общий список")
    void shouldCreateAllHelpers() {
        assertThat(context.getBean(TaskApiClient.class)).isNotNull();
        assertThat(context.getBean(TaskSteps.class)).isNotNull();
        assertThat(context.getBean(DbSteps.class)).isNotNull();
        assertThat(context.getBean(NavigationUiSteps.class)).isNotNull();
        assertThat(context.getBean(TaskUiSteps.class)).isNotNull();
        assertThat(context.getBean(CreatedTasksRegistry.class)).isNotNull();
        Assertions.assertThat(context.getBean(TestProperties.class)).isNotNull();
    }

    @Test
    @DisplayName("singleton: TaskSteps один на все тесты")
    void shouldShareStatelessSteps() {
        assertThat(context.getBean(TaskSteps.class)).isSameAs(context.getBean(TaskSteps.class));
        assertThat(context.getBeansOfType(TaskSteps.class)).hasSize(1);
    }

    @Test
    @DisplayName("prototype: KafkaSteps каждый раз новый")
    void shouldCreateNewKafkaStepsEveryTime() {
        KafkaSteps first = context.getBean(KafkaSteps.class);
        KafkaSteps second = context.getBean(KafkaSteps.class);

        assertThat(first).isNotSameAs(second);
    }
}