package org.example.service;

import org.example.dao.TaskDAO;
import org.example.model.Task;

import java.sql.SQLException;
import java.util.ArrayList;

public class TaskServiceImpl implements TaskService {
    private final TaskDAO taskDAO = new TaskDAO();

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
    public ArrayList<Task> getAllTasks() {
        try {
            return taskDAO.getAll();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar tarefas: " + e.getMessage(), e);
        }
    }
}
