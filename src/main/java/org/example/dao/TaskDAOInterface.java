package org.example.dao;

import org.example.model.Task;

import java.sql.SQLException;
import java.util.List;

public interface TaskDAOInterface {
    void create(Task task) throws SQLException;
    void update(Task task) throws SQLException;
    void delete(int id) throws SQLException;
    Task getById(int id) throws SQLException;
    List<Task> listAll() throws SQLException;
}
