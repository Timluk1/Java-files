package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ignored) {
        }
    }

    private DatabaseConfig() { }

    public static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5433/auth";
        String dbUser = "app";
        String dbPassword = "app";

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
