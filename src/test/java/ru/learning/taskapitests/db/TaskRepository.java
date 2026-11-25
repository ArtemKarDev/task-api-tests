package ru.learning.taskapitests.db;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TaskRepository {

    private static final String TASK_COLUMNS =
            "id, title, description, status, priority, assignee, created_at, updated_at";

    private final DbClient db;
    public TaskRepository(DbClient db){
        this.db = db;
    }
    public Optional<TaskDbRecord> findById(long taskId) {
        return db.jdbc()
                .sql("SELECT " + TASK_COLUMNS + " FROM tasks WHERE id = ?")
                .param(taskId)
                .query(TaskDbRecord.class)
                .optional();
    }

    public long countByTitle(String title) {
        return db.jdbc()
                .sql("SELECT count(*) FROM tasks WHERE title = ?")
                .param(title)
                .query(Long.class)
                .single();
    }

    public long countProcessedMessages(UUID messageId) {
        return db.jdbc()
                .sql("SELECT count(*) FROM processed_messages WHERE message_id = ?")
                .param(messageId)
                .query(Long.class)
                .single();
    }

    public Optional<Long> findTaskIdByMessage(UUID messageId) {
        return db.jdbc()
                .sql("SELECT task_id FROM processed_messages WHERE message_id = ?")
                .param(messageId)
                .query(Long.class)
                .optional();
    }

    public long insert(NewDbTask task) {
        return db.jdbc()
                .sql("""
                        INSERT INTO tasks (title, description, status, priority, assignee, created_at, updated_at)
                        VALUES (:title, :description, :status, :priority, :assignee, :createdAt, :updatedAt)
                        RETURNING id
                        """)
                .param("title", task.title())
                .param("description", task.description())
                .param("status", task.status())
                .param("priority", task.priority())
                .param("assignee", task.assignee())
                .param("createdAt", task.createdAt())
                .param("updatedAt", task.updatedAt())
                .query(Long.class)
                .single();
    }

    public int deleteById(long taskId) {
        return db.jdbc()
                .sql("DELETE FROM tasks WHERE id = ?")
                .param(taskId)
                .update();
    }


}
