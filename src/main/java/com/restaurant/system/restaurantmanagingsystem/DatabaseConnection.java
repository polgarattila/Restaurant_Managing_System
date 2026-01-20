package com.restaurant.system.restaurantmanagingsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String USER = "root";
    private static final String PASSWORD = "admin123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // --- MENÜ ELEMEK ---
    public static boolean addMenuItem(String name, double price, int categoryId) {
        String sql = "INSERT INTO menu_items (name, price, category_id) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, categoryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<MenuItem> getMenuItemsForGUI() {
        List<MenuItem> menuList = new ArrayList<>();
        String sql = "SELECT m.id, m.name, m.price, c.name AS category_name " +
                "FROM menu_items m JOIN categories c ON m.category_id = c.id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                menuList.add(new MenuItem(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getString("category_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return menuList;
    }

    public static boolean updateMenuItem(int id, String name, double price, int categoryId) {
        String sql = "UPDATE menu_items SET name = ?, price = ?, category_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, categoryId);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static void deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- KATEGÓRIÁK ---
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

    public static void addCategory(String name, Integer parentId) {
        String sql = "INSERT INTO categories (name, parent_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            if (parentId != null) pstmt.setInt(2, parentId);
            else pstmt.setNull(2, java.sql.Types.INTEGER);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
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
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    // --- RENDELÉSEK ---
    public static void saveOrder(int tableNumber, List<BasketItem> basket, double total) {
        String findActiveOrderSql = "SELECT id, total_price FROM orders WHERE table_number = ? AND status = 'FOLYAMATBAN'";
        String updateOrderTotalSql = "UPDATE orders SET total_price = total_price + ? WHERE id = ?";
        String insertOrderSql = "INSERT INTO orders (table_number, total_price, status) VALUES (?, ?, 'FOLYAMATBAN')";
        String itemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price_at_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int orderId = -1;

                // 1. Megnézzük, van-e már aktív rendelés az asztalnál
                try (PreparedStatement findPstmt = conn.prepareStatement(findActiveOrderSql)) {
                    findPstmt.setInt(1, tableNumber);
                    ResultSet rs = findPstmt.executeQuery();
                    if (rs.next()) {
                        orderId = rs.getInt("id");
                        // Frissítjük a meglévő rendelés végösszegét
                        try (PreparedStatement updatePstmt = conn.prepareStatement(updateOrderTotalSql)) {
                            updatePstmt.setDouble(1, total);
                            updatePstmt.setInt(2, orderId);
                            updatePstmt.executeUpdate();
                        }
                    }
                }

                // 2. Ha nincs aktív rendelés, létrehozunk egy újat
                if (orderId == -1) {
                    try (PreparedStatement orderPstmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                        orderPstmt.setInt(1, tableNumber);
                        orderPstmt.setDouble(2, total);
                        orderPstmt.executeUpdate();
                        ResultSet rsKey = orderPstmt.getGeneratedKeys();
                        if (rsKey.next()) {
                            orderId = rsKey.getInt(1);
                        }
                    }
                }

                // 3. Az ételeket hozzáadjuk az order_items-hez (akár új, akár régi a rendelés)
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

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
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
                orders.add(new Order(rs.getInt("id"), rs.getInt("table_number"),
                        rs.getTimestamp("order_time").toLocalDateTime(),
                        rs.getString("status"), rs.getDouble("total_price")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public static void updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<ActiveOrderItem> getOrderDetailsForEditing(int orderId) {
        List<ActiveOrderItem> details = new ArrayList<>();
        String sql = "SELECT oi.id, m.name, oi.quantity, oi.price_at_time " +
                "FROM order_items oi " +
                "JOIN menu_items m ON oi.menu_item_id = m.id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    details.add(new ActiveOrderItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price_at_time")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return details;
    }

    // Tétel mennyiségének módosítása
    public static void updateOrderItemQuantity(int itemId, int newQuantity) {
        String sql = "UPDATE order_items SET quantity = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Tétel törlése a rendelésből
    public static void deleteOrderItem(int itemId) {
        String sql = "DELETE FROM order_items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void recalculateOrderTotal(int orderId) {
        // SQL lekérdezés, ami kiszámolja az összeget és rögtön frissíti is a rendelést
        String sql = "UPDATE orders SET total_price = " +
                "(SELECT SUM(quantity * price_at_time) FROM order_items WHERE order_id = ?) " +
                "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}