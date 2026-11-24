package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.client.UiSessionClient;

import static org.assertj.core.api.Assertions.assertThat;


@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Epic("Безопасность")
@Feature("Доступ гостя к страницам")
class GuestAccessTests extends TestBase {

    @Autowired
    private UiSessionClient pages;

    @ParameterizedTest(name = "{0} → {1} {2}")
    @CsvSource({
            "/about,  200, ''",
            "/,       302, /about",
            "/tasks,  302, /login"
    })
    @DisplayName("Гость: открытые страницы и перенаправления")
    void guestAccess(String path, int expectedStatus, String expectedLocationSuffix) {
        Response response = pages.getPageAsGuest(path);

        step.checkStatus(response, expectedStatus);
        if (!expectedLocationSuffix.isEmpty()) {
            assertThat(response.header("Location"))
                    .as("куда перенаправляет %s", path)
                    .endsWith(expectedLocationSuffix);
        }
    }
}
