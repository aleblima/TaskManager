package org.example.service;

import org.example.model.Category;
import java.util.ArrayList;

public interface CategoryService {
    void createCategory(Category category);
    Category getCategoryById(int id);
    ArrayList<Category> getAllCategories();
    void updateCategory(Category category);
    void deleteCategory(int id);
}
