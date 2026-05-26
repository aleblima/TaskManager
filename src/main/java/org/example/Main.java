package org.example;

import org.example.database.Initializer;
import org.example.service.*;
import org.example.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        // Initialize DB Schema
        Initializer.inicializar();

        UserService userService = new UserServiceImpl();
        CategoryService categoryService = new CategoryServiceImpl();
        TaskService taskService = new TaskServiceImpl();

        ConsoleMenu menu = new ConsoleMenu(taskService, userService, categoryService);
        menu.start();
    }
}
