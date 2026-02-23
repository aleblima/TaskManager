import service.TaskService;
import service.UserService;
import ui.ConsoleMenu;

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
