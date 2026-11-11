package ru.learning.taskapitests.extensions;

import io.restassured.response.Response;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.client.AdminApiClient;
import ru.learning.taskapitests.data.TestUser;
import ru.learning.taskapitests.generated.model.CreateUserRequest;
import ru.learning.taskapitests.generated.model.UserResponse;

import java.util.Optional;
import java.util.UUID;

public class UserExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(UserExtension.class);
    private static final String STORE_KEY = "testUser";
    private static final String PASSWORD = "password";


    @Override
    public void beforeEach(ExtensionContext context) {
        Optional<WithUser> annotation = AnnotationSupport.findAnnotation(context.getTestMethod(), WithUser.class);
        if (annotation.isEmpty()) {
            return;
        }
        AdminApiClient adminApi = SpringExtension.getApplicationContext(context).getBean(AdminApiClient.class);
        TestUser user = createUser(adminApi, annotation.get().role());
        context.getStore(NAMESPACE).put(STORE_KEY, user);
    }

    private TestUser createUser(AdminApiClient adminApi, Role role) {
        CreateUserRequest request = new CreateUserRequest()
                .username("qa_" + UUID.randomUUID().toString().substring(0, 8))
                .password(PASSWORD)
                .role(CreateUserRequest.RoleEnum.valueOf(role.name()));

        Response response = adminApi.createUser(request);
        if (response.statusCode() != 201) {
            throw new IllegalStateException("Не удалось создать пользователя для теста: HTTP " + response.statusCode());
        }
        UserResponse created = response.as(UserResponse.class);
        return new TestUser(created.getId(), created.getUsername(), PASSWORD);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestUser user = context.getStore(NAMESPACE).remove(STORE_KEY, TestUser.class);
        if (user == null) {
            return;
        }
        AdminApiClient adminApi = SpringExtension.getApplicationContext(context).getBean(AdminApiClient.class);
        adminApi.deleteUser(user.id());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == TestUser.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        TestUser user = extensionContext.getStore(NAMESPACE).get(STORE_KEY, TestUser.class);
        if (user == null) {
            throw new ParameterResolutionException("Для параметра TestUser нужна аннотация @WithUser на тестовом методе");
        }
        return user;
    }

}
