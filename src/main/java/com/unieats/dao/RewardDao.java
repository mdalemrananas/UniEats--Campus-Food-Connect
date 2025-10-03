package com.unieats.dao;

import java.sql.*;

public class RewardDao {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";

    /**
     * Award reward points to a user for a specific shop
     */
    public void awardRewardPoints(int userId, int shopId, double points) {
        String sql = """
            INSERT INTO reward_points (user_id, shop_id, points) 
            VALUES (?, ?, ?) 
            ON CONFLICT(user_id, shop_id) 
            DO UPDATE SET points = points + ?
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, shopId);
            ps.setDouble(3, points);
            ps.setDouble(4, points);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println("Reward points awarded: " + points + " to user " + userId + " for shop " + shopId + " (rows affected: " + rowsAffected + ")");
            
        } catch (SQLException e) {
            System.err.println("Error awarding reward points: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculate reward points for an order based on food items and their multipliers
     * Points = quantity × points_multiplier (not price)
     */
    public double calculateRewardPoints(int orderId) {
        String sql = """
            SELECT SUM(oi.quantity * fi.points_multiplier) as total_points
            FROM order_items oi
            JOIN food_items fi ON oi.item_id = fi.id
            WHERE oi.order_id = ?
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double points = rs.getDouble("total_points");
                    System.out.println("Calculated reward points for order " + orderId + ": " + points + " (quantity × points_multiplier)");
                    return points;
                }
            }
            return 0.0;
            
        } catch (SQLException e) {
            System.err.println("Error calculating reward points: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Get total reward points for a user at a specific shop
     */
    public double getUserRewardPoints(int userId, int shopId) {
        String sql = "SELECT points FROM reward_points WHERE user_id = ? AND shop_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, shopId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("points");
                }
            }
            return 0.0;
            
        } catch (SQLException e) {
            System.err.println("Error getting user reward points: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all reward points for a user across all shops
     */
    public double getTotalUserRewardPoints(int userId) {
        String sql = "SELECT SUM(points) as total_points FROM reward_points WHERE user_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_points");
                }
            }
            return 0.0;
            
        } catch (SQLException e) {
            System.err.println("Error getting total user reward points: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Redeem reward points (subtract from user's balance)
     */
    public boolean redeemRewardPoints(int userId, int shopId, double pointsToRedeem) {
        String sql = """
            UPDATE reward_points 
            SET points = points - ? 
            WHERE user_id = ? AND shop_id = ? AND points >= ?
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, pointsToRedeem);
            ps.setInt(2, userId);
            ps.setInt(3, shopId);
            ps.setDouble(4, pointsToRedeem);
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                System.out.println("Reward points redeemed: " + pointsToRedeem + " from user " + userId + " for shop " + shopId);
            } else {
                System.out.println("Failed to redeem reward points: insufficient balance or user/shop not found");
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error redeeming reward points: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
