package org.example.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Category;
import org.example.model.Task;
import org.example.model.User;
import org.example.service.CategoryServiceInterface;
import org.example.service.TaskServiceInterface;
import org.example.service.UserServiceInterface;

import java.util.List;

public class TaskFormController {
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<User> userCombo;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private CheckBox statusBox;

    private TaskServiceInterface taskService;
    private UserServiceInterface userService;
    private CategoryServiceInterface categoryService;

    private Task task;
    private MainController parentController;

    public void setServices(TaskServiceInterface taskService, UserServiceInterface userService, CategoryServiceInterface categoryService) {
        this.taskService = taskService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public void setParentController(MainController parent) {
        this.parentController = parent;
    }

    public void setTask(Task task) {
        this.task = task;
        if (task != null) {
            titleField.setText(task.getTitle());
            statusBox.setSelected(task.isStatus());
        }
    }

    @FXML
    public void initialize() {
        // intentionally left blank; services are injected after FXMLLoader.load()
    }

    /**
     * Load data into combos. Call this after setServices().
     */
    public void loadData() {
        try {
            List<User> users = userService.listAllUsers();
            userCombo.setItems(FXCollections.observableArrayList(users));

            List<Category> cats = categoryService.getAllCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(cats));

            if (task == null && (users.isEmpty() || cats.isEmpty())) {
                UiAlerts.warning("Cadastre pelo menos um usuário e uma categoria antes de criar tarefas.");
            }

            // if editing existing task, select values
            if (task != null) {
                // select user
                for (User u : userCombo.getItems()) {
                    if (u.getId() == task.getUser().getId()) {
                        userCombo.getSelectionModel().select(u);
                        break;
                    }
                }
                // select category
                for (Category c : categoryCombo.getItems()) {
                    if (c.getId() == task.getCategoryId()) {
                        categoryCombo.getSelectionModel().select(c);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore for skeleton
        }
    }

    @FXML
    public void handleSave() {
        try {
            String error = FormValidator.validateTask(
                    titleField.getText(),
                    userCombo.getSelectionModel().getSelectedItem(),
                    categoryCombo.getSelectionModel().getSelectedItem()
            );
            if (error != null) {
                UiAlerts.warning(error);
                return;
            }

            if (task == null) {
                User selectedUser = userCombo.getSelectionModel().getSelectedItem();
                Category selectedCat = categoryCombo.getSelectionModel().getSelectedItem();
                Task newTask = new Task(titleField.getText(), 0, selectedUser, selectedCat);
                newTask.setStatus(statusBox.isSelected());
                taskService.createTask(newTask);
            } else {
                task.setTitle(titleField.getText());
                task.setStatus(statusBox.isSelected());
                taskService.updateTask(task);
            }

            if (parentController != null) parentController.refreshTasks();
            closeWindow();
        } catch (Exception e) {
            UiAlerts.error("Erro ao salvar tarefa: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
