package service;

import exception.TaskNotFoundException;
import model.Category;
import model.Task;
import model.User;

import java.util.ArrayList;

public class TaskService {
    private ArrayList<Task> tasks = new ArrayList<>();
    private UserService userService;
    private static int taskid = 0;

    public TaskService(UserService userService){
        this.userService = userService;
    }

    public void createTask(int id, Category category, String name){
        User user = userService.getUserById(id);
        taskid++;
        tasks.add(new Task(name, taskid, user, category));
    }

    public Task getTaskById(int taskid) throws TaskNotFoundException {
        for (Task task : tasks){
            if (task.getId() == taskid){
                return task;
            }
        }
            throw new TaskNotFoundException("Tarefa não encontrada");
    }
    public ArrayList<Task> getAllTasks(){
        return tasks;
    }

    public void taskConcluida(int taskid) throws TaskNotFoundException{
        for (Task task : tasks){
            if (task.getId() == taskid){
                task.setStatus(true);
                return;
            }
        }
        throw new TaskNotFoundException("Tarefa não encontrada");
    }

    public void deleteTask(int taskid) throws TaskNotFoundException{
        boolean removed = tasks.removeIf(task -> task.getId() == taskid);

        if(!removed){
            throw new TaskNotFoundException("Tarefa não encontrada");

        }
    }
}
