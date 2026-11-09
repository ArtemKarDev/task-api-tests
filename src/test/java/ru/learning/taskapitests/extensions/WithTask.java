package ru.learning.taskapitests.extensions;

import org.junit.jupiter.api.extension.ExtendWith;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TaskExtension.class)
public @interface WithTask {
    CreateTaskRequest.StatusEnum status() default CreateTaskRequest.StatusEnum.NEW;
    CreateTaskRequest.PriorityEnum priority() default CreateTaskRequest.PriorityEnum.MEDIUM;
}
