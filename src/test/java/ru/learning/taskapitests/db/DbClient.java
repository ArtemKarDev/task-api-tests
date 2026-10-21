package ru.learning.taskapitests.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbClient {

    public static final String URL = "jdbc:postgresql://localhost:5432/taskdb";

    public static final String USER = "taskuser";

    public static final String PASSWORD = System.getenv().getOrDefault(
            "DB_PASSWORD",
            "local_dev_password"
    );

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private DbClient() {
    }

}
