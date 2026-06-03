package org.example.dao;

import org.example.database.Initializer;
import org.example.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

public class CategoryDAOTest {
    private CategoryDAO categoryDAO;

    @BeforeEach
    public void setup() {
        Initializer.inicializar();
        categoryDAO = new CategoryDAO();
    }

    @Test
    public void testCRUD() throws SQLException {
        Category category = new Category("TestCategory", 0);
        categoryDAO.create(category);
        assertTrue(category.getId() > 0);

        Category fetched = categoryDAO.getById(category.getId());
        assertNotNull(fetched);
        assertEquals("TestCategory", fetched.getCategory());

        List<Category> all = categoryDAO.listAll();
        assertTrue(all.size() > 0);

        // Update
        Category updateCat = new Category("UpdatedTestCategory", category.getId());
        categoryDAO.update(updateCat);

        Category fetchedUpdated = categoryDAO.getById(category.getId());
        assertNotNull(fetchedUpdated);
        assertEquals("UpdatedTestCategory", fetchedUpdated.getCategory());

        // Delete
        categoryDAO.delete(category.getId());
        assertNull(categoryDAO.getById(category.getId()));
    }
}
