package org.example.dao;

import org.example.database.Connect;
import org.example.model.Category;

import java.sql.*;
import java.util.ArrayList;

public class CategoryDAO {

    public void create(Category category) throws SQLException {
        String sql = "INSERT INTO categorias (nome) VALUES (?)";

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getCategory());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
        }
    }

    public Category getById(int id) throws SQLException {
        String sql = "SELECT * FROM categorias WHERE id = ?";

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getString("nome"), rs.getInt("id"));
                }
            }
        }
        return null;
    }

    public ArrayList<Category> getAll() throws SQLException {
        String sql = "SELECT * FROM categorias";
        ArrayList<Category> list = new ArrayList<>();

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(rs.getString("nome"), rs.getInt("id")));
            }
        }
        return list;
    }

    public void atualizar(Category category) throws SQLException {
        String sql = "UPDATE categorias SET nome = ? WHERE id = ?";

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategory());
            stmt.setInt(2, category.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
