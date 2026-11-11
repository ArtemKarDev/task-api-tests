package ru.learning.taskapitests.steps.ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.client.UiSessionClient;
import ru.learning.taskapitests.config.TestProperties;
import ru.learning.taskapitests.ui.pages.LoginPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

import java.util.Map;

import static com.codeborne.selenide.Condition.exactText;
@Component
public class AuthUiSteps {

    private final TestProperties props;
    private final UiSessionClient sessionClient;

    public AuthUiSteps(TestProperties props, UiSessionClient sessionClient) {
        this.props = props;
        this.sessionClient = sessionClient;
    }

    @Step("Войти в интерфейс с ролью {role} (быстро, через cookie сессии)")
    public TasksPage loginAs(Role role) {
        Map<String, String> cookies = sessionClient.login(role);

        // Браузер должен быть на адресе сервиса, иначе он не примет cookie.
        // Страница входа открыта для всех, поэтому подходит.
        Selenide.open(LoginPage.PATH);
        WebDriver driver = WebDriverRunner.getWebDriver();
        cookies.forEach((name, value) -> driver.manage().addCookie(new Cookie(name, value)));

        TasksPage page = new TasksPage().open();
        page.header().userName().shouldHave(exactText(props.auth().accountFor(role).username()));
        return page;
    }

    @Step("Войти в интерфейс с ролью {role} через форму")
    public TasksPage loginThroughForm(Role role) {
        var account = props.auth().accountFor(role);

        TasksPage page = new LoginPage()
                .open()
                .login(account.username(), account.password());

        page.header().userName().shouldHave(exactText(account.username()));
        return page;
    }
}