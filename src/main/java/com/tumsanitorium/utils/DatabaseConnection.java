package com.tumsanitorium.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/tum_sanitorium";
    private static final String USER = "root";
    private static final String PASSWORD = "password"; // Change as per your MySQL setup

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
