package com.restaurant.system.restaurantmanagingsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String USER = "root";
    private static final String PASSWORD = "admin123";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Hiba: " + e.getMessage());
        }
        return connection;
    }

    // Ezt a részt másold át ide a module-info-ból!
    public static void main(String[] args) {
        Connection connection = getConnection();
        if (connection != null) {
            System.out.println("Szuper! A Java és a MySQL sikeresen összekapcsolódott! 🥂");
        } else {
            System.out.println("Valami még nem stimmel. 🧐");
        }
    }
}