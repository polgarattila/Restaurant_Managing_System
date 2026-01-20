package com.restaurant.system.restaurantmanagingsystem;

import java.sql.*;

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

    public static void main(String[] args) {
        if (getConnection() != null) {
            System.out.println("Szuper! A Java és a MySQL sikeresen összekapcsolódott! 🥂");

            // 1. Adatok hozzáadása (elég egyszer lefuttatni)
            addMenuItem("Gulyásleves", 2490, 1);
            addMenuItem("Sült oldalas", 3850, 1);
            addMenuItem("Limonádé", 950, 2);
            addMenuItem("Somlói galuska", 1600, 3);

            // 2. Listázás, hogy lássuk az eredményt
            listCategories();
            listMenuItems();
        }
    }

    public static void addCategory(String name, String description) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);

            pstmt.executeUpdate();
            System.out.println("Kategória hozzáadva: " + name + " ✅");

        } catch (SQLException e) {
            System.out.println("Hiba a mentés során: " + e.getMessage());
        }
    }

    public static void listCategories() {
        String sql = "SELECT id, name, description FROM categories";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("--- Kategóriák listája ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String desc = rs.getString("description");

                System.out.println(id + ". " + name + " - " + desc);
            }

        } catch (SQLException e) {
            System.out.println("Hiba a lekérdezés során: " + e.getMessage());
        }
    }

    public static void listMenuItems() {
        // Itt a 'price'-t kérjük le 'description' helyett
        String sql = "SELECT id, name, price FROM menu_items";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("--- Menü tételek listája ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price"); // double-t használunk az árhoz

                System.out.println(id + ". " + name + " - " + price + " Ft");
            }

        } catch (SQLException e) {
            System.out.println("Hiba a lekérdezés során: " + e.getMessage());
        }
    }

    public static void updateCategory(int id, String newName, String newDescription) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setString(2, newDescription);
            pstmt.setInt(3, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Kategória (ID: " + id + ") sikeresen frissítve! 📝");
            }
        } catch (SQLException e) {
            System.out.println("Hiba a frissítés során: " + e.getMessage());
        }
    }

    public static void deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Kategória (ID: " + id + ") törölve! 🗑️");
            }
        } catch (SQLException e) {
            System.out.println("Hiba a törlés során: " + e.getMessage());
        }
    }

    public static void addMenuItem(String name, double price, int categoryId) {
        String sql = "INSERT INTO menu_items (name, price, category_id) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            // Itt jön az összekötés a kategóriával
            pstmt.setInt(3, categoryId);

            pstmt.executeUpdate();
            System.out.println("Étel hozzáadva: " + name + " ✅");

        } catch (SQLException e) {
            System.out.println("Hiba az étel mentésekor: " + e.getMessage());
        }
    }
}