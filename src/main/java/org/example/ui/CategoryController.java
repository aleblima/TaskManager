package org.example.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Category;
import org.example.service.CategoryServiceInterface;

import java.util.List;

public class CategoryController {
    @FXML
    private ListView<Category> categoryList;

    private final CategoryServiceInterface categoryService = new org.example.service.CategoryService();

    @FXML
    public void initialize() {
        refreshCategories();
    }

    public void refreshCategories() {
        try {
            List<Category> cats = categoryService.getAllCategories();
            categoryList.setItems(FXCollections.observableArrayList(cats));
        } catch (Exception e) {
            // ignore for skeleton
        }
    }

    @FXML
    public void handleNew() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Nova Categoria");
        dlg.setHeaderText("Criar nova categoria");
        dlg.setContentText("Nome:");
        dlg.showAndWait().ifPresent(name -> {
            try {
                String error = FormValidator.validateName(name, "categoria");
                if (error != null) {
                    UiAlerts.warning(error);
                    return;
                }
                Category c = new Category(name, 0);
                categoryService.createCategory(c);
                refreshCategories();
            } catch (Exception e) {
                UiAlerts.error("Erro ao criar categoria: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleEdit() {
        Category sel = categoryList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        TextInputDialog dlg = new TextInputDialog(sel.getCategory());
        dlg.setTitle("Editar Categoria");
        dlg.setHeaderText("Editar categoria");
        dlg.setContentText("Nome:");
        dlg.showAndWait().ifPresent(name -> {
            try {
                String error = FormValidator.validateName(name, "categoria");
                if (error != null) {
                    UiAlerts.warning(error);
                    return;
                }
                sel.setCategory(name);
                categoryService.updateCategory(sel);
                refreshCategories();
            } catch (Exception e) {
                UiAlerts.error("Erro ao editar categoria: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleDelete() {
        Category sel = categoryList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            UiAlerts.warning("Selecione uma categoria para excluir.");
            return;
        }
        if (!UiAlerts.confirm("Deseja realmente excluir a categoria \"" + sel.getCategory() + "\"?")) {
            return;
        }
        categoryService.deleteCategory(sel.getId());
        refreshCategories();
    }
}
