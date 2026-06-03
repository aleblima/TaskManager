package org.example.service;

import org.example.model.Category;
import java.util.List;

public interface CategoryServiceInterface {
    void createCategory(Category category);
    Category getCategoryById(int id);
    List<Category> getAllCategories();
    void updateCategory(Category category);
    void deleteCategory(int id);
}
