package org.example.service;

import org.example.dao.CategoryDAO;
import org.example.dao.CategoryDAOInterface;
import org.example.model.Category;
import java.sql.SQLException;
import java.util.List;

public class CategoryService implements CategoryServiceInterface {
    private final CategoryDAOInterface categoryDAO;

    public CategoryService() {
        this(new CategoryDAO());
    }

    public CategoryService(CategoryDAOInterface categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

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
    public List<Category> getAllCategories() {
        try {
            return categoryDAO.listAll();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar categorias: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateCategory(Category category) {
        try {
            categoryDAO.update(category);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar categoria: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCategory(int id) {
        try {
            categoryDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar categoria: " + e.getMessage(), e);
        }
    }
}
