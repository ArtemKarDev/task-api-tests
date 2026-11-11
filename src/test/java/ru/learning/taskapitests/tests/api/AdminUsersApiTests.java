package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.learning.taskapitests.ApiTest;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.client.AuthApiClient;
import ru.learning.taskapitests.data.TestUser;
import ru.learning.taskapitests.extensions.WithUser;
import ru.learning.taskapitests.generated.model.CreateUserRequest;
import ru.learning.taskapitests.generated.model.TokenResponse;
import ru.learning.taskapitests.generated.model.UpdateUserRequest;
import ru.learning.taskapitests.generated.model.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
@Epic("Авторизация")
@Feature("Управление пользователями")
@DisplayName("Администрирование пользователей")
public class AdminUsersApiTests extends TestBase {

    private static final Logger log = LoggerFactory.getLogger(AdminUsersApiTests.class);

    private static final String ADMIN_USERS_PATH = "/api/admin/users";
    private static final String TASKS_PATH = "/api/tasks";
    private static final String JSON = "application/json";
    private static final String PASSWORD = "password";
    private static final String ERROR_TOKEN = "Invalid or expired token";
    private static final String ERROR_CREDENTIALS = "Invalid username or password";

    /** Пользователи, созданные этим тестом. У каждого теста свой объект класса, поэтому список не общий. */
    private final List<Long> createdUserIds = new ArrayList<>();

    @AfterEach
    void deleteCreatedUsers() {
        log.info("Очистка: пользователи на удаление {}", createdUserIds);
        for (long id : createdUserIds) {
            adminApi.deleteUser(id);
        }
        createdUserIds.clear();
    }

    @Test
    @DisplayName("Успешное создание пользователя")
    void shouldCreateUser() {
        UserResponse user = createUser(Role.USER);

        assertThat(user.getId()).as("Номер пользователя положителен").isPositive();
        assertThat(user.getUsername()).as("Логин из ответа").startsWith("qa_");
        assertThat(user.getRole().name()).as("Роль созданного пользователя").isEqualTo(Role.USER.name());
        assertThat(user.getEnabled()).as("Созданный пользователь включён").isTrue();

        assertThat(listUsers()).as("Созданный пользователь есть в списке").contains(user);
    }

    @ParameterizedTest(name = "[{index}] роль {0} -> {1}")
    @CsvSource({
            "USER, 403",
            "ADMIN, 200"
    })
    @DisplayName("Созданный пользователь получает права своей роли")
    void shouldApplyRolePermissionsToCreatedUser(Role role, int expectedStatus) {
        UserResponse user = createUser(role);
        String token = loginAndGetToken(user.getUsername(),PASSWORD);

        Response response = step.sendRequestWithToken(token, "GET", ADMIN_USERS_PATH, JSON, null);

        step.checkStatus(response, expectedStatus);
    }

    @Test
    @WithUser
    @DisplayName("Отключённый пользователь теряет доступ сразу")
    void shouldLoseAccessImmediatelyAfterDisable(TestUser user) {

        String token = loginAndGetToken(user.username(), user.password());

        checkTokenAccepted(token);

        disableUser(user.id());

        checkTokenRejected(token);
        checkLoginRejected(user.username());
    }

    @Step("Создать пользователя с ролью {role}")
    private UserResponse createUser(Role role) {
        CreateUserRequest request = new CreateUserRequest()
                .username("qa_" + UUID.randomUUID().toString().substring(0, 8))
                .password(PASSWORD)
                .role(CreateUserRequest.RoleEnum.valueOf(role.name()));

        Response response = adminApi.createUser(request);
        step.checkStatus(response, 201);

        UserResponse created = response.as(UserResponse.class);
        createdUserIds.add(created.getId());
        return created;
    }

    @Step("Получить список пользователей")
    private List<UserResponse> listUsers() {
        Response response = adminApi.listUsers();
        step.checkStatus(response, 200);
        return List.of(response.as(UserResponse[].class));
    }

    @Step("Войти как {username} и получить токен")
    private String loginAndGetToken(String username, String password) {
        Response response = authApi.login(username, password);
        step.checkStatus(response, 200);
        return response.as(TokenResponse.class).getAccessToken();
    }

    @Step("Отключить пользователя {userId}")
    private void disableUser(long userId) {
        Response response = adminApi.updateUser(userId, new UpdateUserRequest().enabled(false));
        step.checkStatus(response, 200);
        assertThat(response.as(UserResponse.class).getEnabled())
                .as("В ответе пользователь отключён").isFalse();
    }

    @Step("Проверить, что токен принимается")
    private void checkTokenAccepted(String token) {
        Response response = step.sendRequestWithToken(token, "GET", TASKS_PATH, JSON, null);
        step.checkStatus(response, 200);
    }

    @Step("Проверить, что токен отклоняется")
    private void checkTokenRejected(String token) {
        Response response = step.sendRequestWithToken(token, "GET", TASKS_PATH, JSON, null);
        step.checkApiError(response, 401, ERROR_TOKEN, TASKS_PATH);
    }

    @Step("Проверить, что вход закрыт для {username}")
    private void checkLoginRejected(String username) {
        Response response = authApi.login(username, PASSWORD);
        step.checkApiError(response, 401, ERROR_CREDENTIALS, AuthApiClient.LOGIN_PATH);
    }
}