package ru.learning.taskapitests.db;

import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DbClient {

    private final TestProperties props;

    public DbClient(TestProperties props) {
        this.props = props;
    }

    public Connection getConnection() throws SQLException {
        String password = props.db().password();
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Не задан пароль БД. Задайте переменную окружения TESTS_DB_PASSWORD "
                    + "или параметр -Dtests.db.password=... (для профиля local пароль лежит в application-local.yml)");
        }
        return DriverManager.getConnection(props.db().url(), props.db().user(), password);
    }
}