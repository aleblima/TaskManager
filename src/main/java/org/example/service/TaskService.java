package org.example.service;

import org.example.dao.TaskDAO;
import org.example.dao.TaskDAOInterface;
import org.example.model.Task;

import java.sql.SQLException;
import java.util.List;

public class TaskService implements TaskServiceInterface {
    private final TaskDAOInterface taskDAO;

    public TaskService() {
        this(new TaskDAO());
    }

    public TaskService(TaskDAOInterface taskDAO) {
        this.taskDAO = taskDAO;
    }

    @Override
    public void createTask(Task task) {
        try{
            taskDAO.create(task);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tarefa: " + e.getMessage(), e);
        }
    }

    @Override
    public Task getTaskById(int id) {
        try {
            Task task = taskDAO.getById(id);
            if (task == null) throw new RuntimeException("Tarefa não encontrada");
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar tarefa: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateTask(Task task) {
        try {
            taskDAO.update(task);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar tarefa: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTask(int id) {
        try {
            taskDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar tarefa: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Task> getAllTasks() {
        try {
            return taskDAO.listAll();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar tarefas: " + e.getMessage(), e);
        }
    }
}
