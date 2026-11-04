package ru.learning.taskapitests.steps;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class CreatedTasksRegistry {

    private final ThreadLocal<List<Long>> ids = ThreadLocal.withInitial(ArrayList::new);

    public void register(long taskId){
        ids.get().add(taskId);
    }

    public List<Long> snapshot() {
        return List.copyOf(ids.get());
    }

    public void clear(){
        ids.get().clear();
    }
}
