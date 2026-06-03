package org.example.service;

import org.example.model.Task;
import java.util.List;

public interface TaskServiceInterface {
    void createTask(Task task);
    Task getTaskById(int id);
    void updateTask(Task task);
    void deleteTask(int id);
    List<Task> getAllTasks();
}
