package ru.learning.taskapitests.extensions;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EmployeeExtension.class)
public @interface WithEmployee {

    State value() default State.EXISTS;

    enum State {
        /** Справочник отвечает 200. */
        EXISTS,
        /** Справочник отвечает 404. */
        NOT_FOUND,
        /** Справочник отвечает 503. */
        DIRECTORY_ERROR,
        /** Справочник отвечает 200, но дольше таймаута сервиса. */
        DIRECTORY_SLOW
    }
}