package org.example.service;

import org.example.dao.CategoryDAO;
import org.example.model.Category;
import java.sql.SQLException;
import java.util.ArrayList;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public void createCategory(Category category) {
        try {
            categoryDAO.create(category);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar categoria: " + e.getMessage(), e);
        }
    }

    @Override
    public Category getCategoryById(int id) {
        try {
            return categoryDAO.getById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar categoria: " + e.getMessage(), e);
        }
    }

    @Override
    public ArrayList<Category> getAllCategories() {
        try {
            return categoryDAO.getAll();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar categorias: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateCategory(Category category) {
        try {
            categoryDAO.atualizar(category);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar categoria: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCategory(int id) {
        try {
            categoryDAO.deletar(id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar categoria: " + e.getMessage(), e);
        }
    }
}
