package org.example;


import org.example.service.TaskService;
import org.example.service.UserService;
import org.example.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
    // Inicialização dos serviços (Injeção de Dependência)
    UserService userService = new UserService();
    TaskService taskService = new TaskService(userService);

    // Inicialização da UI Interativa
    ConsoleMenu menu = new ConsoleMenu(taskService, userService);

        // Iniciar o menu principal
        menu.start();
    }
}
