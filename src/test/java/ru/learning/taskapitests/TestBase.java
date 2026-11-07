package ru.learning.taskapitests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.learning.taskapitests.auth.TokenProvider;
import ru.learning.taskapitests.client.AuthApiClient;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.config.TestProperties;
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
    @Autowired
    protected TestProperties props;
    @Autowired
    protected TokenProvider tokenProvider;
    @Autowired
    protected AuthApiClient authApi;

    @BeforeEach
    protected void setUp() {
        tokenProvider.reset();
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
