package org.example.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.example.database.Connect;
import org.example.database.Initializer;
import org.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserDAOTest {
    private User buildUser() {
        return new User("TestUser", 0);
    }

    private UserDAO userDAO;

    private User createUserPersistent() throws SQLException {
        User u = buildUser();
        userDAO.create(u);
        return u;
    }

    @BeforeEach
    public void setUp() throws SQLException {
        Initializer.inicializar();

        try (Connection conn = Connect.getConnect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM tarefas");
            stmt.executeUpdate("DELETE FROM categorias");
            stmt.executeUpdate("DELETE FROM usuarios");
        }

        userDAO = new UserDAO();
    }

    @Test
    public void createUser() throws SQLException {
        User u = buildUser();
        userDAO.create(u);
        assertTrue(u.getId() > 0);
    }

    @Test
    public void getById() throws SQLException {
        User u = createUserPersistent();
        User fetched = userDAO.getById(u.getId());
        assertNotNull(fetched);
        assertEquals(u.getNome(), fetched.getNome());
    }

    @Test
    public void listAll() throws SQLException {
        User u1 = new User("TestUser1", 0);
        User u2 = new User("TestUser2", 0);
        userDAO.create(u1);
        userDAO.create(u2);

        List<User> users = userDAO.listAll();
        assertNotNull(users);
        assertTrue(users.stream().anyMatch(u -> u.getNome().equals("TestUser1")));
        assertTrue(users.stream().anyMatch(u -> u.getNome().equals("TestUser2")));
    }

    @Test
    public void updateUser() throws SQLException {
        User u = createUserPersistent();
        // mutate the persisted object and update it
        User toUpdate = userDAO.getById(u.getId());
        assertNotNull(toUpdate);
        toUpdate.setNome("UpdatedUser");
        userDAO.update(toUpdate);

        User fetched = userDAO.getById(u.getId());
        assertNotNull(fetched);
        assertEquals("UpdatedUser", fetched.getNome());
    }

    @Test
    public void deleteUser() throws SQLException {
        User u = createUserPersistent();
        userDAO.delete(u.getId());
        assertNull(userDAO.getById(u.getId()));
    }
}
