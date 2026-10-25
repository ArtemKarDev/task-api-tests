package ru.learning.taskapitests.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.db.DbClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Tag(Tags.DB)
@DisplayName("Тест подключения к БД")
public class DbConnectionTests {

    @Test
    void shouldConnectionDatabase() throws Exception{
        try (Connection connection = DbClient.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            assertThat(connection.isValid(2)).isTrue();

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(1);

        }
    }
}
