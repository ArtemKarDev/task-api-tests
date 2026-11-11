package ru.learning.taskapitests.extensions;

import org.junit.jupiter.api.extension.ExtendWith;
import ru.learning.taskapitests.auth.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(UserExtension.class)
public @interface WithUser {
    Role role() default Role.USER;

}
