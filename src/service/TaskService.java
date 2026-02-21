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
//                Colocar na UI System.out.printf("Dados da tarefa:%nNome: %s%nId: %d%nCategoria: %s%nStatus: %s%n",task.getTitle(), task.getId(), task.getCategory(), task.getStatus());
            }
        }
            throw new TaskNotFoundException("Tarefa não encontrada");
    }
    public ArrayList<Task> getAllTasks(){
        return tasks;
    }

}
