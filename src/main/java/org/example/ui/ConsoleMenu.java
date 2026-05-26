package org.example.ui;

import org.example.model.Category;
import org.example.model.Task;
import org.example.model.User;
import org.example.service.CategoryService;
import org.example.service.TaskService;
import org.example.service.UserService;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ConsoleMenu {
    private final TaskService taskService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final Scanner scanner;

    public ConsoleMenu(TaskService taskService, UserService userService, CategoryService categoryService) {
        this.taskService = taskService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== GERENCIADOR DE TAREFAS ===");
            System.out.println("1. Criar Usuário");
            System.out.println("2. Criar Tarefa");
            System.out.println("3. Listar Todas as Tarefas");
            System.out.println("4. Consultar Tarefa por ID");
            System.out.println("5. Marcar Tarefa como Concluída");
            System.out.println("6. Excluir Tarefa");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> handleCreateUser();
                case 2 -> handleCreateTask();
                case 3 -> listAllTasks();
                case 4 -> handleConsultTask();
                case 5 -> handleUpdateStatus();
                case 6 -> handleDeleteTask();
                case 0 -> {
                    System.out.println("Saindo... Até logo!");
                    running = false;
                }
                default -> System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    private void handleCreateUser() {
        System.out.print("Nome do novo usuário: ");
        String name = scanner.nextLine();
        User user = new User(name, 0);
        userService.createUser(user);
        System.out.println("Usuário criado: " + user.getNome() + ", id: " + user.getId());
    }

    private void handleCreateTask() {
        System.out.print("ID do Usuário responsável: ");
        int userId = readInt();
        System.out.print("Nome da tarefa: ");
        String taskName = scanner.nextLine();
        System.out.print("Nome da categoria: ");
        String catName = scanner.nextLine();

        try {
            User user = userService.getUserById(userId);
            
            // Find or create Category
            Category category = null;
            ArrayList<Category> categories = categoryService.getAllCategories();
            for (Category cat : categories) {
                if (cat.getCategory().equalsIgnoreCase(catName)) {
                    category = cat;
                    break;
                }
            }
            if (category == null) {
                category = new Category(catName, 0);
                categoryService.createCategory(category);
            }

            Task task = new Task(taskName, 0, user, category);
            taskService.createTask(task);
            System.out.println("Tarefa criada com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void handleConsultTask() {
        System.out.print("ID da tarefa para consulta: ");
        int id = readInt();
        displayTaskDetails(id);
    }

    private void handleUpdateStatus() {
        System.out.print("ID da tarefa concluída: ");
        int id = readInt();
        try {
            Task task = taskService.getTaskById(id);
            task.setStatus(true);
            taskService.updateTask(task);
            System.out.println("Status atualizado!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void handleDeleteTask() {
        System.out.print("ID da tarefa para excluir: ");
        int id = readInt();
        try {
            taskService.deleteTask(id);
            System.out.println("Tarefa removida com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void displayTaskDetails(int taskId) {
        try {
            Task task = taskService.getTaskById(taskId);
            System.out.println("\n--- Detalhes da Tarefa ---");
            System.out.printf("ID: %d%n", task.getId());
            System.out.printf("Nome: %s%n", task.getTitle());
            System.out.printf("Categoria: %s%n", task.getCategory());
            System.out.printf("Status: %s%n", task.getStatus());
            System.out.println("--------------------------");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void listAllTasks() {
        ArrayList<Task> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("\nNenhuma tarefa cadastrada.");
            return;
        }
        System.out.println("\n--- Lista de Todas as Tarefas ---");
        for (Task task : tasks) {
            System.out.printf("[%d] %s (%s) - %s%n", 
                task.getId(), task.getTitle(), task.getCategory(), task.getStatus());
        }
    }

    private int readInt() {
        while (true) {
            try {
                int value = scanner.nextInt();
                scanner.nextLine(); // Limpar o buffer do teclado
                return value;
            } catch (InputMismatchException e) {
                System.out.print("Entrada inválida! Por favor, digite um número: ");
                scanner.nextLine(); // Limpar o buffer de erro
            }
        }
    }
}
