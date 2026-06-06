package org.example.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.example.database.Connect;
import org.example.database.Initializer;
import org.example.model.Category;
import org.example.model.Task;
import org.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskDAOTest {
    private User buildUser() {
        return new User("UserTeste", 0);
    }
    private Category buildCategory() {
        return new Category("CategoriaTeste", 0);
    }
    private Task buildTask(User user, Category category) {
        return new Task("TarefaTeste", 0, user, category);
    }
    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private TaskDAO taskDAO;

    private Task createTaskPersistent() throws SQLException {
        User user = buildUser();
        Category category = buildCategory();
        Task task = buildTask(user, category);
        userDAO.create(user);
        categoryDAO.create(category);
        taskDAO.create(task);

        return task;
    }

    @BeforeEach
    public void setup() throws SQLException {
        Initializer.inicializar();

        try (Connection conn = Connect.getConnect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM tarefas");
            stmt.executeUpdate("DELETE FROM categorias");
            stmt.executeUpdate("DELETE FROM usuarios");
        }

        userDAO = new UserDAO();
        categoryDAO = new CategoryDAO();
        taskDAO = new TaskDAO();
    }

    @Test
    public void createTask() throws SQLException {
        User user = buildUser();
        userDAO.create(user);
        Category category = buildCategory();
        categoryDAO.create(category);
        Task task = buildTask(user, category);
        taskDAO.create(task);

        assertTrue(task.getId() > 0);
    }
    @Test
    public void getById() throws SQLException {
        Task task = createTaskPersistent();
        Task fetched = taskDAO.getById(task.getId());
        assertNotNull(fetched);
        assertEquals("TarefaTeste", fetched.getTitle());
    }

    @Test
    public void getAll() throws SQLException {
        Task task = createTaskPersistent();
        Task task2 = createTaskPersistent();
        List<Task> fetched = taskDAO.listAll();
        assertNotNull(fetched);
        assertTrue(fetched.stream().anyMatch(t -> t.getTitle().equals(task.getTitle())));
        assertTrue(fetched.stream().anyMatch(t -> t.getTitle().equals(task2.getTitle())));

    }

    @Test
    public void updateTask() throws SQLException {
        Task task = createTaskPersistent();
        task.setTitle("New task");
        task.setStatus(true);
        taskDAO.update(task);
        Task newTask = taskDAO.getById(task.getId());
        assertEquals("New task", newTask.getTitle());
        assertTrue(newTask.isStatus());
    }
    @Test
    public void deleteTask() throws SQLException {
        Task task = createTaskPersistent();
        taskDAO.delete(task.getId());
        assertNull(taskDAO.getById(task.getId()));
    }
}
