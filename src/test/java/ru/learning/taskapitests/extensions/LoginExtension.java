package ru.learning.taskapitests.extensions;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.steps.ui.AuthUiSteps;

import java.util.Optional;

public class LoginExtension implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        findRole(context).ifPresent(role -> SpringExtension.getApplicationContext(context)
                .getBean(AuthUiSteps.class)
                .loginAs(role));
    }

    private Optional<Role> findRole(ExtensionContext context) {
        Optional<LoginAs> onMethod = AnnotationSupport.findAnnotation(context.getTestMethod(), LoginAs.class);
        if (onMethod.isPresent()) {
            return onMethod.map(LoginAs::value);
        }
        return AnnotationSupport.findAnnotation(context.getTestClass(), LoginAs.class)
                .map(LoginAs::value);
    }
}