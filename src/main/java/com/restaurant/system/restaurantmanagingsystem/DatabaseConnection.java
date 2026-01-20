package com.restaurant.system.restaurantmanagingsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        listMenuItemsWithCategories();
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

    public static void listMenuItemsWithCategories() {
        String sql = "SELECT m.name, m.price, c.name AS category_name " +
                "FROM menu_items m " +
                "JOIN categories c ON m.category_id = c.id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("--- Étlap kategóriákkal együtt ---");
            while (rs.next()) {
                String itemName = rs.getString("name");
                double price = rs.getDouble("price");
                String categoryName = rs.getString("category_name");

                System.out.println(itemName + " [" + categoryName + "] - " + price + " Ft");
            }

        } catch (SQLException e) {
            System.out.println("Hiba a JOIN lekérdezés során: " + e.getMessage());
        }
    }

    public static List<MenuItem> getMenuItemsForGUI() {
        List<MenuItem> menuList = new ArrayList<>();
        String sql = "SELECT m.id, m.name, m.price, c.name AS category_name " +
                "FROM menu_items m " +
                "JOIN categories c ON m.category_id = c.id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Létrehozunk egy tárgyat az adatbázis sorából
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category_name")
                );
                // Hozzáadjuk a listához
                menuList.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Hiba az adatok lekérésekor: " + e.getMessage());
        }
        return menuList;
    }
}