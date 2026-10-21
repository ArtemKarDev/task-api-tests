package ru.learning.taskapitests.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskRepository {
    public TaskDbRecord findById(long taskId) throws SQLException {
        String sql = """
                SELECT id, title, description, status , priority
                FROM tasks
                WHERE id = ?                
                """;

        try (Connection connection = DbClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1, taskId);

            try(ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
                    return null;
                }

                return new TaskDbRecord(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("status"),
                        resultSet.getString("priority")
                );
            }
        }
    }
}
