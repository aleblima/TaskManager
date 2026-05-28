package org.example.dao;

import org.example.database.Initializer;
import org.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.ArrayList;

public class UserDAOTest {
    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        Initializer.inicializar();
        userDAO = new UserDAO();
    }

    @Test
    public void testListAll() throws SQLException {
        User u1 = new User("TestUser1", 0);
        User u2 = new User("TestUser2", 0);
        userDAO.create(u1);
        userDAO.create(u2);

        ArrayList<User> users = userDAO.listAll();
        assertNotNull(users);
        assertTrue(users.size() >= 2);
        
        boolean found1 = users.stream().anyMatch(u -> u.getNome().equals("TestUser1"));
        boolean found2 = users.stream().anyMatch(u -> u.getNome().equals("TestUser2"));
        assertTrue(found1);
        assertTrue(found2);
    }
}
