package ru.learning.taskapitests.db;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.config.TestProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DbClient {

    private final TestProperties props;
    private final DataSource dataSource;
    private final JdbcClient jdbcClient;

    public DbClient(TestProperties props, DataSource dataSource) {
        this.props = props;
        this.dataSource = dataSource;
        this.jdbcClient = JdbcClient.create(dataSource);
    }

    public JdbcClient jdbc() {
        checkPassword();
        return jdbcClient;
    }

    public Connection getConnection() throws SQLException {
        checkPassword();
        return dataSource.getConnection();
    }
    private void checkPassword() {
        String password = props.db().password();
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Не задан пароль БД. Задайте переменную окружения TESTS_DB_PASSWORD "
                    + "или параметр -Dtests.db.password=... (для профиля local пароль лежит в application-local.yml)");
        }
    }



}