package ru.learning.taskapitests.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import ru.learning.taskapitests.ui.components.TaskRow;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TasksPage extends BasePage {

    public static final String PATH = "/tasks";

    public static final String ALL_STATUSES = "";

    private final SelenideElement searchInput = $("[data-testid=search-input]");
    private final SelenideElement statusFilter = $("[data-testid=status-filter]");
    private final SelenideElement searchButton = $("[data-testid=search-btn]");
    private final SelenideElement flash = $("[data-testid=flash]");
    private final SelenideElement emptyState = $("[data-testid=empty-state]");
    private final ElementsCollection rows = $$("[data-testid=task-row]");

    public TasksPage open() {
        Selenide.open(PATH);
        return this;
    }

    public TasksPage search(String query) {
        searchButton.setValue(query);
        searchButton.click();
        return this;
    }

    public TasksPage filterByStatus(String status) {
        statusFilter.selectOptionByValue(status);
        searchButton.click();
        return this;
    }

    public TasksPage searchWithStatus(String query, String status) {
        searchInput.setValue(query);
        statusFilter.selectOptionByValue(status);
        searchButton.click();
        return this;
    }

    public ElementsCollection rows() {
        return rows;
    }

    public ElementsCollection titleCells() {
        return $$("[data-testid=task-title]");
    }

    public TaskRow rowByTitle(String title) {
        SelenideElement row = rows.findBy(Condition.match(
                "строка с названием '" + title + "'",
                element -> title.equals(element.findElement(By.cssSelector("[data-testid=task-title]")).getText())));
        return new TaskRow(row);
    }

    public TaskRow rowById(long id) {
        return new TaskRow($("tr[data-testid=task-row][data-id='" + id + "']"));
    }


    public SelenideElement searchInput() {
        return searchInput;
    }

    public SelenideElement statusFilter() {
        return statusFilter;
    }

    public SelenideElement flash() {
        return flash;
    }

    public SelenideElement emptyState() {
        return emptyState;
    }

}
