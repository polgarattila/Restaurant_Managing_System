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
        String sql = "SELECT id, name, parent_id FROM categories";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name"),
                        (Integer) rs.getObject("parent_id")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
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

    public static void addCategory(String name, Integer parentId) {
        String sql = "INSERT INTO categories (name, parent_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            if (parentId != null) pstmt.setInt(2, parentId);
            else pstmt.setNull(2, java.sql.Types.INTEGER);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCategory(int id, String name, Integer parentId) {
        String sql = "UPDATE categories SET name = ?, parent_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            if (parentId != null) pstmt.setInt(2, parentId);
            else pstmt.setNull(2, java.sql.Types.INTEGER);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true; // Sikerült
        } catch (SQLException e) {
            // Ha az ErrorCode 1451, akkor az idegen kulcs korlátozás miatt nem törölhető
            System.out.println("Hiba a törlésnél: " + e.getMessage());
            return false; // Nem sikerült (pl. vannak benne ételek)
        }
    }

    public static void saveOrder(int tableNumber, List<BasketItem> basket, double total) {
        String orderSql = "INSERT INTO orders (table_number, total_price) VALUES (?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price_at_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Tranzakció indítása

            try (PreparedStatement orderPstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderPstmt.setInt(1, tableNumber);
                orderPstmt.setDouble(2, total);
                orderPstmt.executeUpdate();

                ResultSet rs = orderPstmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    try (PreparedStatement itemPstmt = conn.prepareStatement(itemSql)) {
                        for (BasketItem bi : basket) {
                            itemPstmt.setInt(1, orderId);
                            itemPstmt.setInt(2, bi.getItem().getId());
                            itemPstmt.setInt(3, bi.getQuantity());
                            itemPstmt.setDouble(4, bi.getItem().getPrice());
                            itemPstmt.addBatch();
                        }
                        itemPstmt.executeBatch();
                    }
                }
                conn.commit(); // Ha minden jó, mentjük
            } catch (SQLException e) {
                conn.rollback(); // Hiba esetén visszavonjuk az egészet
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Order> getActiveOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = 'FOLYAMATBAN' ORDER BY order_time DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getTimestamp("order_time").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getDouble("total_price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static void updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Hiba a rendelés állapotának frissítésekor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}