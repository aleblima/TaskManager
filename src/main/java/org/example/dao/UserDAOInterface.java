package org.example.dao;

import org.example.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDAOInterface {
    void create(User user) throws SQLException;
    void update(User user) throws SQLException;
    void delete(int id) throws SQLException;
    User getById(int id) throws SQLException;
    List<User> listAll() throws SQLException;
}
