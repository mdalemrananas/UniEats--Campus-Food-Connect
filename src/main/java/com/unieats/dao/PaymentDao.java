package com.unieats.dao;

import java.sql.*;

public class PaymentDao {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";

    /**
     * Create a new payment record
     */
    public int createPayment(int orderId, String paymentMethod, double amount, String transactionId) {
        String sql = "INSERT INTO payments(order_id, payment_method, amount, transaction_id, status) VALUES(?,?,?,?,?)";
        String selectSql = "SELECT last_insert_rowid()";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("Creating payment record: orderId=" + orderId + ", method=" + paymentMethod + ", amount=" + amount + ", transactionId=" + transactionId);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                ps.setString(2, paymentMethod);
                ps.setDouble(3, amount);
                ps.setString(4, transactionId);
                ps.setString(5, "pending");
                int rowsAffected = ps.executeUpdate();
                System.out.println("Payment insert affected " + rowsAffected + " rows");
            }
            try (PreparedStatement ps = conn.prepareStatement(selectSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int paymentId = rs.getInt(1);
                    System.out.println("Payment created with ID: " + paymentId);
                    com.unieats.services.EventNotifier.notifyChange("payments");
                    return paymentId;
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Update payment status
     */
    public void updatePaymentStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get payment by order ID
     */
    public PaymentInfo getPaymentByOrderId(int orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PaymentInfo(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getString("payment_method"),
                        rs.getDouble("amount"),
                        rs.getString("status"),
                        rs.getString("transaction_id"),
                        rs.getString("payment_details"),
                        rs.getString("created_at")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * List latest payments
     */
    public ResultSet listLatest(Connection external, int limit) throws SQLException {
        String sql = "SELECT * FROM payments ORDER BY created_at DESC LIMIT ?";
        PreparedStatement ps = external.prepareStatement(sql);
        ps.setInt(1, limit);
        return ps.executeQuery();
    }

    /**
     * Get total sum of all payments
     */
    public double getTotalPaymentsSum() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE status IN ('completed', 'success')";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
        } catch (SQLException e) {
            System.err.println("Error getting total payments sum: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Payment information class
     */
    public static class PaymentInfo {
        public final int id;
        public final int orderId;
        public final String paymentMethod;
        public final double amount;
        public final String status;
        public final String transactionId;
        public final String paymentDetails;
        public final String createdAt;

        public PaymentInfo(int id, int orderId, String paymentMethod, double amount, 
                          String status, String transactionId, String paymentDetails, String createdAt) {
            this.id = id;
            this.orderId = orderId;
            this.paymentMethod = paymentMethod;
            this.amount = amount;
            this.status = status;
            this.transactionId = transactionId;
            this.paymentDetails = paymentDetails;
            this.createdAt = createdAt;
        }
    }
}
