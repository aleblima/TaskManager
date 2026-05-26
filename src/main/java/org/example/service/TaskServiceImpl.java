package org.example.service;

import org.example.model.Task;
import java.util.ArrayList;

public class TaskServiceImpl implements TaskService {
    private ArrayList<Task> tasks = new ArrayList<>();
    private static int taskid = 0;

    @Override
    public void createTask(Task task) {
        taskid++;
        // set generated ID for the in-memory task
        Task newTask = new Task(task.getTitle(), taskid, task.getUser(), new org.example.model.Category(task.getCategory(), 0));
        tasks.add(newTask);
    }

    @Override
    public Task getTaskById(int id) {
        for (Task task : tasks){
            if (task.getId() == id){
                return task;
            }
        }
        throw new RuntimeException("Tarefa não encontrada");
    }

    @Override
    public void updateTask(Task task) {
        for (Task t : tasks){
            if (t.getId() == task.getId()){
                t.setTitle(task.getTitle());
                t.setStatus("Concluída".equals(task.getStatus()));
                return;
            }
        }
        throw new RuntimeException("Tarefa não encontrada");
    }

    @Override
    public void deleteTask(int id) {
        boolean removed = tasks.removeIf(task -> task.getId() == id);
        if(!removed){
            throw new RuntimeException("Tarefa não encontrada");
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return tasks;
    }
}
