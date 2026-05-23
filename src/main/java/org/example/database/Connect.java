package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {

    private static final String URL = "jdbc:sqlite:taskmanager.db";
    public static Connection getConnect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
