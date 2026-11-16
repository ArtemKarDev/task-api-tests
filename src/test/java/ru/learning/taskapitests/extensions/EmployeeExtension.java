package ru.learning.taskapitests.extensions;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.learning.taskapitests.client.DirectoryStub;

import java.util.UUID;

public class EmployeeExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(EmployeeExtension.class);
    private static final String LOGIN_KEY = "login";
    private static final String STUB_KEY = "stub";

    private static final int SLOW_DELAY_MS = 5000;
    private static final int ERROR_STATUS = 503;

    @Override
    public void beforeEach(ExtensionContext context) {
        WithEmployee annotation = AnnotationSupport
                .findAnnotation(context.getRequiredTestMethod(), WithEmployee.class)
                .orElse(null);
        if (annotation == null) {
            return;
        }

        String login = "emp-" + UUID.randomUUID().toString().substring(0, 8);
        DirectoryStub directoryStub = directoryStub(context);

        StubMapping stub = switch (annotation.value()) {
            case EXISTS -> directoryStub.employeeExists(login);
            case NOT_FOUND -> directoryStub.employeeNotFound(login);
            case DIRECTORY_ERROR -> directoryStub.directoryFails(login, ERROR_STATUS);
            case DIRECTORY_SLOW -> directoryStub.directorySlow(login, SLOW_DELAY_MS);
        };

        ExtensionContext.Store store = context.getStore(NAMESPACE);
        store.put(LOGIN_KEY, login);
        store.put(STUB_KEY, stub);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        StubMapping stub = context.getStore(NAMESPACE).get(STUB_KEY, StubMapping.class);
        if (stub != null) {
            directoryStub(context).remove(stub);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == TestEmployee.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        String login = extensionContext.getStore(NAMESPACE).get(LOGIN_KEY, String.class);
        if (login == null) {
            throw new IllegalStateException("Параметр TestEmployee доступен только в тесте с @WithEmployee");
        }
        return new TestEmployee(login);
    }

    private static DirectoryStub directoryStub(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context).getBean(DirectoryStub.class);
    }
}