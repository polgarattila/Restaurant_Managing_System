package com.restaurant.system.restaurantmanagingsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String USER = "root";
    private static final String PASSWORD = "admin123"; // Írd be a saját jelszavad!

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean addMenuItem(String name, double price, int categoryId) {
        String sql = "INSERT INTO menu_items (name, price, category_id) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Tranzakció indítása

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, price);
                pstmt.setInt(3, categoryId);
                pstmt.executeUpdate();

                conn.commit(); // Ha minden jó, véglegesítjük
                return true;
            } catch (SQLException e) {
                if (conn != null) conn.rollback(); // Hiba esetén visszavonjuk
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Adatbázis hiba: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
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
                menuList.add(new MenuItem(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getString("category_name")));
            }
        } catch (SQLException e) {
            System.out.println("Hiba a lekérdezésnél: " + e.getMessage());
        }
        return menuList;
    }

    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM categories";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.out.println("Hiba a kategóriák lekérésekor: " + e.getMessage());
        }
        return categories;
    }

    public static void deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Hiba a törlésnél: " + e.getMessage());
        }
    }

    public static boolean updateMenuItem(int id, String name, double price, int categoryId) {
        String sql = "UPDATE menu_items SET name = ?, price = ?, category_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, categoryId);
            pstmt.setInt(4, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Hiba a módosítás során: " + e.getMessage());
            return false;
        }
    }
}