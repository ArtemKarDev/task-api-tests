package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.client.AuthApiClient;
import ru.learning.taskapitests.config.TestProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Epic("API")
@Feature("Учебный пользователь")
class DemoAccountApiTests extends TestBase {

    private static final String DEMO_FORBIDDEN = "This action is not allowed for the demo account";

    @Autowired
    private AuthApiClient authApi;

    @Test
    @DisplayName("Учебный пользователь не может сменить пароль через API: 403, пароль прежний")
    void demoCannotChangePasswordViaApi() {
        TestProperties.Account demo = props.auth().demo();
        assumeTrue(demo.password() != null && !demo.password().isBlank(),
                "Пароль учебного пользователя не задан (tests.auth.demo.password)");
        String token = authApi.loginAndGetToken(demo.username(), demo.password());

        Response response = authApi.changeOwnPassword(token, demo.password(), "Another-pass-1234");

        step.checkApiError(response, 403, DEMO_FORBIDDEN, "/api/users/me/password");
        assertThat(authApi.loginAndGetToken(demo.username(), demo.password()))
                .as("вход прежним паролем работает")
                .isNotBlank();
    }
}

