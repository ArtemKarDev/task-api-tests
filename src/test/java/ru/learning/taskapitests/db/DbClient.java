package ru.learning.taskapitests.db;

import ru.learning.taskapitests.config.TestConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbClient {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                TestConfig.getDbUrl(),
                TestConfig.getDbUser(),
                TestConfig.getDbPassword()
        );
    }

    private DbClient() {
    }

}
