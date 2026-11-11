package ru.learning.taskapitests.extensions;


import org.junit.jupiter.api.extension.ExtendWith;
import ru.learning.taskapitests.auth.Role;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(LoginExtension.class)
public @interface LoginAs {
    Role value() default Role.USER;
}