package ru.learning.taskapitests.client;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;

import java.net.URI;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Управление заглушкой справочника сотрудников (WireMock) через её admin API.
 * Каждая заглушка создаётся для одного логина и удаляется тестом, который её создал
 */
@Component
public class DirectoryStub {

    private static final int TEST_PRIORITY = 1;
    private final WireMock wireMock;

    public DirectoryStub(TestProperties props){
        URI uri = URI.create(props.wiremock().url());
        if(uri.getPort() == -1) {
            throw new IllegalStateException("В tests.wiremock.uri нужен порт, сейчас: " + uri);
        }
        this.wireMock = new WireMock(uri.getScheme(), uri.getHost(), uri.getPort());
    }

    @Step("Справочник: сотрудник {login} существует")
    public StubMapping employeeExists(String login) {
        return stubLookup(login, aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"active\":true}"));
    }

    @Step("Справочник: сотрудник {login} не найден (404)")
    public StubMapping employeeNotFound(String login) {
        return stubLookup(login, aResponse().withStatus(404));
    }

    @Step("Справочник: ответ {status} для {login}")
    public StubMapping directoryFails(String login, int status) {
        return stubLookup(login, aResponse().withStatus(status));
    }

    @Step("Справочник: ответ для {login} с задержкой {delayMs} мс")
    public StubMapping directorySlow(String login, int delayMs) {
        return stubLookup(login, aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"active\":true}")
                .withFixedDelay(delayMs));
    }

    @Step("Справочник: удалить заглушку")
    public void remove(StubMapping stub) {
        wireMock.removeStubMapping(stub);
    }

    /** Запросы GET /employees/{login}, дошедшие до заглушки. */
    public List<LoggedRequest> lookups(String login) {
        return wireMock.find(getRequestedFor(urlPathEqualTo(employeePath(login))));
    }
    private StubMapping stubLookup(String login, ResponseDefinitionBuilder response) {
        return wireMock.register(get(urlPathEqualTo(employeePath(login)))
                .atPriority(TEST_PRIORITY)
                .willReturn(response));
    }
    private static String employeePath(String login) {
        return "/employees/" + login;
    }

}
