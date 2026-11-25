package ru.learning.taskapitests.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.db.NewDbTask;
import ru.learning.taskapitests.db.TaskDbRecord;
import ru.learning.taskapitests.db.TaskRepository;

import java.time.LocalDateTime;

public class DbTaskExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(DbTaskExtension.class);
    private static final String TASK_KEY = "dbTask";

    @Override
    public void beforeEach(ExtensionContext context) {
        WithDbTask annotation = AnnotationSupport
                .findAnnotation(context.getRequiredTestMethod(), WithDbTask.class)
                .orElse(null);
        if (annotation == null) {
            return;
        }

        LocalDateTime when = LocalDateTime.now().minusDays(annotation.createdDaysAgo());
        NewDbTask newTask = new NewDbTask(
                TaskDataFactory.uniqueTitle("Db fixture"),
                "Создана напрямую в БД",
                annotation.status().getValue(),
                annotation.priority().getValue(),
                null,
                when,
                when);

        TaskRepository repository = repository(context);
        long id = repository.insert(newTask);
        TaskDbRecord saved = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Задача " + id + " не прочиталась после вставки"));
        context.getStore(NAMESPACE).put(TASK_KEY, saved);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TaskDbRecord task = context.getStore(NAMESPACE).get(TASK_KEY, TaskDbRecord.class);
        if (task != null) {
            repository(context).deleteById(task.id());
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == TaskDbRecord.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        TaskDbRecord task = extensionContext.getStore(NAMESPACE).get(TASK_KEY, TaskDbRecord.class);
        if (task == null) {
            throw new IllegalStateException("Параметр TaskDbRecord доступен только в тесте с @WithDbTask");
        }
        return task;
    }

    private static TaskRepository repository(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context).getBean(TaskRepository.class);
    }
}