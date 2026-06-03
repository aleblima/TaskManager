package org.example;

import org.example.database.Initializer;
import org.example.service.CategoryServiceInterface;
import org.example.service.TaskServiceInterface;
import org.example.service.UserServiceInterface;
import org.example.service.CategoryService;
import org.example.service.TaskService;
import org.example.service.UserService;
import org.example.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        // Initialize DB Schema
        Initializer.inicializar();

        UserServiceInterface userService = new UserService();
        CategoryServiceInterface categoryService = new CategoryService();
        TaskServiceInterface taskService = new TaskService();

        ConsoleMenu menu = new ConsoleMenu(taskService, userService, categoryService);
        menu.start();
    }
}
