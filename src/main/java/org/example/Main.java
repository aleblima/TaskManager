package org.example;


import org.example.service.TaskService;
import org.example.service.UserService;
import org.example.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
    UserService userService = new UserService();
    TaskService taskService = new TaskService(userService);

    ConsoleMenu menu = new ConsoleMenu(taskService, userService);

        menu.start();
    }
}
