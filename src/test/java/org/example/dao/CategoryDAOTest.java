package org.example.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.example.database.Connect;
import org.example.database.Initializer;
import org.example.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CategoryDAOTest {
    private CategoryDAO categoryDAO;

    private Category buildCategory() {
        return new Category("TestCategory", 0);
    }

    private Category createCategoryPersistent() throws SQLException {
        Category c = buildCategory();
        categoryDAO.create(c);
        return c;
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

        categoryDAO = new CategoryDAO();
    }

    @Test
    public void createCategory() throws SQLException {
        Category c = buildCategory();
        categoryDAO.create(c);
        assertTrue(c.getId() > 0);
    }

    @Test
    public void getById() throws SQLException {
        Category c = createCategoryPersistent();
        Category fetched = categoryDAO.getById(c.getId());
        assertNotNull(fetched);
        assertEquals(c.getCategory(), fetched.getCategory());
    }

    @Test
    public void listAll() throws SQLException {
        Category c1 = new Category("Cat1", 0);
        Category c2 = new Category("Cat2", 0);
        categoryDAO.create(c1);
        categoryDAO.create(c2);

        List<Category> all = categoryDAO.listAll();
        assertNotNull(all);
        assertTrue(all.stream().anyMatch(cat -> cat.getCategory().equals("Cat1")));
        assertTrue(all.stream().anyMatch(cat -> cat.getCategory().equals("Cat2")));
    }

    @Test
    public void updateCategory() throws SQLException {
        Category c = createCategoryPersistent();
        // mutate the persisted object and update it
        Category toUpdate = categoryDAO.getById(c.getId());
        assertNotNull(toUpdate);
        toUpdate.setCategory("UpdatedCat");
        categoryDAO.update(toUpdate);

        Category fetched = categoryDAO.getById(c.getId());
        assertNotNull(fetched);
        assertEquals("UpdatedCat", fetched.getCategory());
    }

    @Test
    public void deleteCategory() throws SQLException {
        Category c = createCategoryPersistent();
        categoryDAO.delete(c.getId());
        assertNull(categoryDAO.getById(c.getId()));
    }
}
