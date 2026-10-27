package ru.learning.taskapitests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.steps.DbSteps;
import ru.learning.taskapitests.steps.TaskSteps;

public class TestBase {

    protected TaskApiClient taskApi;
    protected TaskSteps step;
    protected DbSteps dbStep;

    @BeforeEach
    protected void setUp() {
        taskApi = new TaskApiClient();
        step = new TaskSteps(taskApi);
        step.clearTaskIdInCreatedList();
        dbStep = new DbSteps();
    }


    @AfterEach
    protected void cleanUp() {
        step.cleanDbCreatedTasks();
    }


}
