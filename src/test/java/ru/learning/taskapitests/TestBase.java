package ru.learning.taskapitests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.config.TestsConfig;
import ru.learning.taskapitests.steps.DbSteps;
import ru.learning.taskapitests.steps.KafkaSteps;
import ru.learning.taskapitests.steps.TaskSteps;

@SpringBootTest(classes = TestsConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TestBase {

    @Autowired
    protected TaskApiClient taskApi;
    @Autowired
    protected TaskSteps step;
    @Autowired
    protected DbSteps dbStep;
    @Autowired
    protected KafkaSteps kafkaStep;

    @BeforeEach
    protected void setUp() {
        step.clearTaskIdInCreatedList();
    }


    @AfterEach
    protected void cleanUp() {
        try {
            step.cleanDbCreatedTasks();
        } finally {
            kafkaStep.close();
        }
    }


}
