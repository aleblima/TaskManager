package org.example.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Task;
import org.example.service.TaskServiceInterface;
import org.example.service.CategoryServiceInterface;
import org.example.service.UserServiceInterface;

import java.io.IOException;
import java.util.List;

public class MainController {
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> titleCol;
    @FXML
    private TableColumn<Task, String> statusCol;
    @FXML
    private TableColumn<Task, String> userCol;
    @FXML
    private TableColumn<Task, String> categoryCol;
    @FXML
    private Label statusLabel;

    // Services - simple direct instantiation for the skeleton
    private final TaskServiceInterface taskService = new org.example.service.TaskService();
    private final UserServiceInterface userService = new org.example.service.UserService();
    private final CategoryServiceInterface categoryService = new org.example.service.CategoryService();

    @FXML
    public void initialize() {
        if (taskTable == null) {
            return;
        }
        // configure columns
        titleCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTitle()));
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        userCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUser().getNome()));
        categoryCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategory()));

        refreshTasks();
    }

    public void refreshTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            taskTable.setItems(FXCollections.observableArrayList(tasks));
            statusLabel.setText(String.format("Total: %d", tasks.size()));
        } catch (Exception e) {
            statusLabel.setText("Erro ao carregar tarefas: " + e.getMessage());
        }
    }

    @FXML
    public void handleNewTask() {
        openTaskForm(null);
    }

    @FXML
    public void handleEditTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected != null) openTaskForm(selected);
    }

    @FXML
    public void handleDeleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                taskService.deleteTask(selected.getId());
                refreshTasks();
            } catch (Exception e) {
                statusLabel.setText("Erro ao deletar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleOpenCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Categorias");
            stage.showAndWait();
            refreshTasks();
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir categorias: " + e.getMessage());
        }
    }

    @FXML
    public void handleOpenUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Usuários");
            stage.showAndWait();
            refreshTasks();
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir usuários: " + e.getMessage());
        }
    }

    private void openTaskForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskForm.fxml"));
            Parent root = loader.load();
            TaskFormController controller = loader.getController();
            controller.setServices(taskService, userService, categoryService);
            controller.setParentController(this);
            controller.setTask(task);
            controller.loadData();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle(task == null ? "Nova Tarefa" : "Editar Tarefa");
            stage.showAndWait();
            refreshTasks();
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir formulário: " + e.getMessage());
        }
    }
}
