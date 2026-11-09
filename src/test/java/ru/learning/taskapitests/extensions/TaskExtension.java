package ru.learning.taskapitests.extensions;

import io.restassured.response.Response;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.learning.taskapitests.client.TaskApiClient;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.generated.model.Task;

import java.util.Optional;

public class TaskExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(TaskExtension.class);

    private static final String STORE_KEY = "testTask";

    @Override
    public void beforeEach(ExtensionContext context) {
        Optional<WithTask> annotation = AnnotationSupport.findAnnotation(context.getTestMethod(), WithTask.class);
        if (annotation.isEmpty()) {
            return;
        }

        TaskApiClient taskApi = SpringExtension.getApplicationContext(context).getBean(TaskApiClient.class);
        Task task = createTask(taskApi, annotation.get());
        context.getStore(NAMESPACE).put(STORE_KEY, task);
    }

    private Task createTask(TaskApiClient taskApi, WithTask spec) {
        CreateTaskRequest request = TaskDataFactory.customTaskRequestDTO(r -> {
            r.title(TaskDataFactory.uniqueTitle("Fixture"));
            r.status(spec.status());
            r.priority(spec.priority());
        });

        Response response = taskApi.createTaskDto(request);
        if (response.statusCode() != 201) {
            throw new IllegalStateException("Не удалось создать задачу для теста: HTTP " + response.statusCode());
        }
        return response.as(Task.class);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Task task = context.getStore(NAMESPACE).remove(STORE_KEY, Task.class);
        if (task == null) {
            return;
        }
        TaskApiClient taskApi = SpringExtension.getApplicationContext(context).getBean(TaskApiClient.class);
        taskApi.deleteTask(task.getId());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == Task.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Task task = extensionContext.getStore(NAMESPACE).get(STORE_KEY, Task.class);
        if (task == null) {
            throw new ParameterResolutionException("Для параметра Task нужна аннотация @WithTask на тестовом методе");
        }
        return task;
    }
}
