package ru.learning.taskapitests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.steps.DbSteps;
import ru.learning.taskapitests.steps.KafkaSteps;
import ru.learning.taskapitests.steps.TaskSteps;

public class TestBase {

    protected TaskApiClient taskApi;
    protected TaskSteps step;
    protected DbSteps dbStep;
    protected KafkaSteps kafkaStep;

    @BeforeEach
    protected void setUp() {
        taskApi = new TaskApiClient();
        step = new TaskSteps(taskApi);
        step.clearTaskIdInCreatedList();
        dbStep = new DbSteps();
        kafkaStep = new KafkaSteps();
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
