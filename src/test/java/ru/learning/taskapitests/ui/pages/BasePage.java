package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.SelenideElement;
import ru.learning.taskapitests.ui.components.Header;

import static com.codeborne.selenide.Selenide.$;

public class BasePage {
    private final Header header = new Header();

    public Header header() {
        return header;
    }

    public SelenideElement heading() {
        return $("h1");
    }


}
