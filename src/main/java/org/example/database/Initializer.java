package org.example.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Initializer {

    public static void inicializar() {
        String sql = """
                PRAGMA foreign_keys = ON;
                
                CREATE TABLE IF NOT EXISTS usuarios (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome VARCHAR(100) NOT NULL
                );
                
                CREATE TABLE IF NOT EXISTS categorias (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome VARCHAR(100) NOT NULL
                );
                
                CREATE TABLE IF NOT EXISTS tarefas (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    titulo       VARCHAR(100) NOT NULL,
                    concluida    BOOLEAN      NOT NULL DEFAULT 0,
                    usuario_id   INTEGER      NOT NULL,
                    categoria_id INTEGER      NOT NULL,
                    FOREIGN KEY (usuario_id)   REFERENCES usuarios(id),
                    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
                );
                """;

        try (Connection conn = Connect.getConnect();
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            for (String comando : sql.split(";")) {
                String trimmed = comando.trim();
                if (!trimmed.isEmpty()) {
                    stmt.addBatch(trimmed);
                }
            }

            stmt.executeBatch();
            conn.commit();
            System.out.println("Banco inicializado com sucesso.");

        } catch (SQLException e) {
            System.out.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }
}