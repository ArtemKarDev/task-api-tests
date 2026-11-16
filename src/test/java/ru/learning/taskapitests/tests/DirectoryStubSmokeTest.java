package ru.learning.taskapitests.tests;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.client.DirectoryStub;
import ru.learning.taskapitests.config.TestProperties;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryStubSmokeTest extends TestBase {

    @Autowired
    private DirectoryStub directoryStub;

    @Autowired
    private TestProperties props;

    @Test
    void stubLifecycle() {
        String login = "emp-" + UUID.randomUUID().toString().substring(0, 8);

        StubMapping stub = directoryStub.employeeNotFound(login);
        assertThat(lookupStatus(login)).as("ответ с нашим правилом").isEqualTo(404);
        assertThat(directoryStub.lookups(login)).as("запросов в журнале").hasSize(1);

        directoryStub.remove(stub);
        assertThat(lookupStatus(login)).as("ответ после удаления правила").isEqualTo(200);
    }

    private int lookupStatus(String login) {
        return RestAssured.given()
                .baseUri(props.wiremock().url())
                .get("/employees/" + login)
                .statusCode();
    }
}