package org.example.dao;

import org.example.database.Connect;
import org.example.model.User;

import java.sql.*;

public class UserDAO {

    public class UsuarioDAO {

        public void create(User user) throws SQLException {
            String sql = "INSERT INTO usuarios (nome) VALUES (?)";

            try (Connection conn = Connect.getConnect();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, user.getNome());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
            }
        }

        public User getById(int id) throws SQLException {
            String sql = "SELECT * FROM usuarios WHERE id = ?";

            try (Connection conn = Connect.getConnect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(rs.getString("nome"), rs.getInt("id"));
                    }
                }
            }
            return null;
        }

        public User getByName(String nome) throws SQLException {
            String sql = "SELECT * FROM usuarios WHERE nome = ?";

            try (Connection conn = Connect.getConnect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, nome);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(rs.getString("nome"), rs.getInt("id"));
                    }
                }
            }
            return null;
        }

        public void atualizar(User user) throws SQLException {
            String sql = "UPDATE usuarios SET nome = ? WHERE id = ?";

            try (Connection conn = Connect.getConnect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, user.getNome());
                stmt.setInt(2, user.getId());
                stmt.executeUpdate();
            }
        }

        public void deletar(int id) throws SQLException {
            String sql = "DELETE FROM usuarios WHERE id = ?";

            try (Connection conn = Connect.getConnect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        }
    }
}
