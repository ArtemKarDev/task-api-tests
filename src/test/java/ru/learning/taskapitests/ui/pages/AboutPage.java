package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class AboutPage extends BasePage {

    public static final String PATH = "/about";

    @Step("Открыть страницу «О проекте»")

    public AboutPage open() {
        Selenide.open(PATH);
        return this;
    }

    public SelenideElement content() {
        return $("[data-testid=about-content]");
    }

    public SelenideElement authorName() {
        return $("#author h1");
    }

    public SelenideElement section(String id) {
        return $("section#" + id);
    }

    public ElementsCollection repoLinks() {
        return $$("#source a.about-repo");
    }

    public SelenideElement swaggerLink() {
        return $("#try a[href$='/swagger-ui/index.html']");
    }

    /** Блок учебного входа: есть, только если на стенде включён demo. */
    public SelenideElement demoBlock() {
        return $("#try .about-demo");
    }

    public SelenideElement demoUsername() {
        return demoBlock().$$("dd").get(0);
    }

    public SelenideElement demoPassword() {
        return demoBlock().$$("dd").get(1);
    }

    @Step("Войти как гость")
    public TasksPage loginAsGuest() {
        demoBlock().$("form[action$='/login'] button[type=submit]").click();
        return new TasksPage();
    }



}