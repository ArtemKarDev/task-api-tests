package ru.learning.taskapitests.testdata;

import ru.learning.taskapitests.models.CreateTaskRequest;
public class TaskTestData {

    public static CreateTaskRequest validCreateTaskRequest(){
        return new CreateTaskRequest(
                "Автотестовая задача",
                "Создана через REST Assured",
                "NEW",
                "HIGH",
                "tester");
    }

}
