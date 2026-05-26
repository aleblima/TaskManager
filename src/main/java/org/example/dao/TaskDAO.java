package org.example.dao;

import org.example.database.Connect;
import org.example.model.Category;
import org.example.model.Task;
import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;

public class TaskDAO {

    public void create(Task task) throws SQLException {
        String sql = "INSERT INTO tarefas (titulo, concluida, usuario_id, categoria_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, task.getTitle());
            stmt.setBoolean(2, task.isStatus());
            stmt.setInt(3, task.getUser().getId());
            stmt.setInt(4, task.getCategoryId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
        }
    }

    public Task getById(int id) throws SQLException {
        String sql = """
                SELECT t.id, t.titulo, t.concluida,
                       u.id AS usuario_id, u.nome AS usuario_nome,
                       c.id AS categoria_id, c.nome AS categoria_nome
                FROM tarefas t
                JOIN usuarios u ON t.usuario_id = u.id
                JOIN categorias c ON t.categoria_id = c.id
                WHERE t.id = ?
                """;

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ArrayList<Task> getAll() throws SQLException {
        String sql = """
                SELECT t.id, t.titulo, t.concluida,
                       u.id AS usuario_id, u.nome AS usuario_nome,
                       c.id AS categoria_id, c.nome AS categoria_nome
                FROM tarefas t
                JOIN usuarios u ON t.usuario_id = u.id
                JOIN categorias c ON t.categoria_id = c.id
                """;

        ArrayList<Task> list = new ArrayList<>();

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void update(Task task) throws SQLException {
        String sql = "UPDATE tarefas SET titulo = ?, concluida = ? WHERE id = ?";

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setBoolean(2, task.isStatus());
            stmt.setInt(3, task.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tarefas WHERE id = ?";

        try (Connection conn = Connect.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        User user = new User(rs.getString("usuario_nome"), rs.getInt("usuario_id"));
        Category category = new Category(rs.getString("categoria_nome"), rs.getInt("categoria_id"));
        Task task = new Task(rs.getString("titulo"), rs.getInt("id"), user, category);
        task.setStatus(rs.getBoolean("concluida"));
        return task;
    }
}
