package org.example.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import org.example.model.User;
import org.example.service.UserServiceInterface;

import java.util.List;

public class UserController {
    @FXML
    private ListView<User> userList;

    private final UserServiceInterface userService = new org.example.service.UserService();

    @FXML
    public void initialize() {
        refreshUsers();
    }

    public void refreshUsers() {
        try {
            List<User> users = userService.listAllUsers();
            userList.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            // skeleton only
        }
    }

    @FXML
    public void handleNew() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Novo Usuário");
        dlg.setHeaderText("Criar novo usuário");
        dlg.setContentText("Nome:");
        dlg.showAndWait().ifPresent(name -> {
            try {
                userService.createUser(new User(name, 0));
                refreshUsers();
            } catch (Exception ignored) {}
        });
    }

    @FXML
    public void handleEdit() {
        User sel = userList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        TextInputDialog dlg = new TextInputDialog(sel.getNome());
        dlg.setTitle("Editar Usuário");
        dlg.setHeaderText("Editar usuário");
        dlg.setContentText("Nome:");
        dlg.showAndWait().ifPresent(name -> {
            try {
                sel.setNome(name);
                userService.updateUser(sel);
                refreshUsers();
            } catch (Exception ignored) {}
        });
    }

    @FXML
    public void handleDelete() {
        User sel = userList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        userService.deleteUser(sel.getId());
        refreshUsers();
    }
}
