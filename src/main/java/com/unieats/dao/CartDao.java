package com.unieats.dao;

import java.sql.*;

public class CartDao {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";

    /**
     * Check if a specific item is already present in user's cart
     */
    public boolean isInCart(int userId, int itemId) {
        String sql = "SELECT 1 FROM cart WHERE user_id=? AND item_id=? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /**
     * Get the shop ID of the first item in the user's cart
     * @param userId the user ID
     * @return shop ID of first cart item, or -1 if cart is empty
     */
    public int getFirstCartItemShopId(int userId) {
        String sql = """
            SELECT fi.shop_id 
            FROM cart c 
            JOIN food_items fi ON c.item_id = fi.id 
            WHERE c.user_id = ? 
            ORDER BY c.id 
            LIMIT 1
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shop_id");
                }
            }
            return -1; // Cart is empty
        } catch (SQLException e) { 
            throw new RuntimeException(e); 
        }
    }

    /**
     * Get the shop ID of a specific food item
     * @param itemId the food item ID
     * @return shop ID of the food item, or -1 if not found
     */
    public int getFoodItemShopId(int itemId) {
        String sql = "SELECT shop_id FROM food_items WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shop_id");
                }
            }
            return -1; // Item not found
        } catch (SQLException e) { 
            throw new RuntimeException(e); 
        }
    }

    public void addToCart(int userId, int itemId, int quantity) {
        // Check shop restriction: only allow items from the same shop as the first cart item
        int firstCartShopId = getFirstCartItemShopId(userId);
        int itemShopId = getFoodItemShopId(itemId);
        
        if (firstCartShopId != -1 && firstCartShopId != itemShopId) {
            throw new RuntimeException("You can only add items from the same shop. Please clear your cart first to add items from a different shop.");
        }
        
        String select = "SELECT quantity FROM cart WHERE user_id=? AND item_id=?";
        String insert = "INSERT INTO cart(user_id,item_id,quantity) VALUES(?,?,?)";
        String update = "UPDATE cart SET quantity = quantity + ? WHERE user_id=? AND item_id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, userId);
                ps.setInt(2, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement up = conn.prepareStatement(update)) {
                            up.setInt(1, quantity);
                            up.setInt(2, userId);
                            up.setInt(3, itemId);
                            up.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ins = conn.prepareStatement(insert)) {
                            ins.setInt(1, userId);
                            ins.setInt(2, itemId);
                            ins.setInt(3, quantity);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /**
     * Update quantity by delta. If resulting quantity <= 0, remove the row.
     */
    public void updateQuantity(int userId, int itemId, int delta) {
        String select = "SELECT quantity FROM cart WHERE user_id=? AND item_id=?";
        String update = "UPDATE cart SET quantity=? WHERE user_id=? AND item_id=?";
        String delete = "DELETE FROM cart WHERE user_id=? AND item_id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, userId);
                ps.setInt(2, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return; // nothing to do
                    int q = rs.getInt(1) + delta;
                    if (q <= 0) {
                        try (PreparedStatement del = conn.prepareStatement(delete)) {
                            del.setInt(1, userId);
                            del.setInt(2, itemId);
                            del.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement up = conn.prepareStatement(update)) {
                            up.setInt(1, q);
                            up.setInt(2, userId);
                            up.setInt(3, itemId);
                            up.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void clearCart(int userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE user_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /**
     * Remove a specific item from the user's cart
     */
    public void removeFromCart(int userId, int itemId) {
        String sql = "DELETE FROM cart WHERE user_id=? AND item_id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
