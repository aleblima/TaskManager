package org.example.service;

import org.example.model.Task;
import java.util.ArrayList;

public interface TaskService {
    void createTask(Task task);
    Task getTaskById(int id);
    void updateTask(Task task);
    void deleteTask(int id);
    ArrayList<Task> getAllTasks();
}
