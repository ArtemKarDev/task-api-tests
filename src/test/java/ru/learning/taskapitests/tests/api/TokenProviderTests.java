package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.auth.Role;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Epic("Авторизация")
@Feature("Вход")
@DisplayName("Получение токена")
public class TokenProviderTests extends TestBase {

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource({"USER,user", "ADMIN,admin"})
    @DisplayName("Получение токена для роли")
    public void shouldGetToken(Role role, String expectedLogin) {

        String token = tokenProvider.login(role);

        assertThat(token).as("Ответ на запрос токена пришёл").isNotBlank();

        JsonPath jsonToken = getCredFromJwt(token);

        assertThat(jsonToken.getString("sub"))
                .as("Логин из токена").isEqualTo(expectedLogin);
        assertThat(jsonToken.getString("role"))
                .as("Роль из токена").isEqualTo(role.name());
    }

    private JsonPath getCredFromJwt(String token) {
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        byte[] decodeCreds = Base64.getUrlDecoder().decode(parts[1]);
        String payloadJson = new String(decodeCreds, StandardCharsets.UTF_8);
        return new JsonPath(payloadJson);
    }

    @Test
    @DisplayName("Повторный запрос берёт токен из кэша")
    public void shouldReturnSameTokenForSecondTry() {
        tokenProvider.reset();
        String token1 = tokenProvider.tokenFor(Role.USER);
        String token2 = tokenProvider.tokenFor(Role.USER);

        assertThat(token1).as("Повторный вызов вернул тот же объект из кэша").isSameAs(token2);
    }

    @Test
    @DisplayName("Разные роли дают разные токены")
    public void shouldReturnOtherTokenForOtherRole() {
        tokenProvider.reset();
        String token1 = tokenProvider.tokenFor(Role.USER);
        String token2 = tokenProvider.tokenFor(Role.ADMIN);

        assertThat(token1).as("Токены разных ролей различаются").isNotEqualTo(token2);
    }

    @Test
    @DisplayName("После reset() вход выполняется заново")
    public void shouldReturnOtherTokenAfterReset() {
        tokenProvider.reset();
        String token1 = tokenProvider.tokenFor(Role.USER);
        tokenProvider.reset();
        String token2 = tokenProvider.tokenFor(Role.USER);

        assertThat(token1).as("После reset() токен получен новым входом").isNotSameAs(token2);

    }


}
