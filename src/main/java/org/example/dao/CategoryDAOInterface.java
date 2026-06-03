package org.example.dao;

import org.example.model.Category;

import java.sql.SQLException;
import java.util.List;

public interface CategoryDAOInterface {
    void create(Category category) throws SQLException;
    void update(Category category) throws SQLException;
    void delete(int id) throws SQLException;
    Category getById(int id) throws SQLException;
    List<Category> listAll() throws SQLException;
}
