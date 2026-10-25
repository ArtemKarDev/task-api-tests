package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class AboutPage extends BasePage {

    public static final String PATH = "/about";

    public AboutPage open() {
        Selenide.open(PATH);
        return this;
    }

    public SelenideElement content() {
        return $("[data-testid=about-content]");
    }
}