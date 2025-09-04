package com.unieats.dao;

import java.sql.*;

public class CartDao {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";

    public void addToCart(int userId, int itemId, int quantity) {
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
}
