package ru.learning.taskapitests.ui.components;

import com.codeborne.selenide.SelenideElement;
import ru.learning.taskapitests.ui.pages.AboutPage;
import ru.learning.taskapitests.ui.pages.LoginPage;
import ru.learning.taskapitests.ui.pages.TasksPage;

import static com.codeborne.selenide.Selenide.$;

public class Header {

    private final SelenideElement root = $("header.site-header");

    public SelenideElement brand() {
        return root.$("[data-testid=brand]");
    }

    public SelenideElement searchLink() {
        return root.$("[data-testid=nav-search]");
    }

    public SelenideElement createLink() {
        return root.$("nav a[href*='tasks/new']");
    }

    public SelenideElement aboutLink() {
        return root.$("[data-testid=nav-about]");
    }

    public SelenideElement loginLink() {
        return root.$("a.login-link");
    }

    public SelenideElement activeLink() {
        return root.$("nav a.active");
    }

    public TasksPage openSearch() {
        searchLink().click();
        return new TasksPage();
    }

    public AboutPage openAbout() {
        aboutLink().click();
        return new AboutPage();
    }

    public LoginPage openLogin() {
        loginLink().click();
        return new LoginPage();
    }


}
