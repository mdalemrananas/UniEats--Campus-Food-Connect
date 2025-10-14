package com.unieats.dao;

import com.unieats.OrderRequest;
import com.unieats.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRequestDao {
    private static final String DB_URL = "jdbc:sqlite:unieats.db";
    
    public int createOrderRequest(OrderRequest orderRequest) {
        String sql = "INSERT INTO order_requests(customer_id, customer_name, shop_id, total_price, status, order_time, updated_at) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, orderRequest.getCustomerId());
            ps.setString(2, orderRequest.getCustomerName());
            ps.setInt(3, orderRequest.getShopId());
            ps.setDouble(4, orderRequest.getTotalPrice());
            ps.setString(5, orderRequest.getStatus());
            ps.setString(6, orderRequest.getOrderTime().toString());
            ps.setString(7, orderRequest.getUpdatedAt().toString());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    
                    // Insert order items
                    insertOrderItems(orderId, orderRequest.getItems());
                    
                    return orderId;
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order request", e);
        }
    }
    
    private void insertOrderItems(int orderId, List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, food_item_id, food_item_name, quantity, unit_price, total_price) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (OrderItem item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getFoodItemId());
                ps.setString(3, item.getFoodItemName());
                ps.setInt(4, item.getQuantity());
                ps.setDouble(5, item.getUnitPrice());
                ps.setDouble(6, item.getTotalPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    
    public List<OrderRequest> getPendingOrdersByShopId(int shopId) {
        String sql = "SELECT * FROM order_requests WHERE shop_id = ? AND status = 'pending' ORDER BY order_time DESC";
        List<OrderRequest> orders = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, shopId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderRequest order = mapOrderRequest(rs);
                    order.setItems(getOrderItems(order.getId()));
                    orders.add(order);
                }
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get pending orders", e);
        }
    }
    
    public int getPendingOrderCount(int shopId) {
        String sql = "SELECT COUNT(*) FROM order_requests WHERE shop_id = ? AND status = 'pending'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, shopId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get pending order count", e);
        }
    }
    
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE order_requests SET status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setInt(3, orderId);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status", e);
        }
    }
    
    private List<OrderItem> getOrderItems(int orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapOrderItem(rs));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get order items", e);
        }
    }
    
    private OrderRequest mapOrderRequest(ResultSet rs) throws SQLException {
        OrderRequest order = new OrderRequest();
        order.setId(rs.getInt("id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setShopId(rs.getInt("shop_id"));
        order.setTotalPrice(rs.getDouble("total_price"));
        order.setStatus(rs.getString("status"));
        
        try {
            order.setOrderTime(LocalDateTime.parse(rs.getString("order_time")));
            order.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
        } catch (Exception ignored) {}
        
        return order;
    }
    
    private OrderItem mapOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id"));
        item.setFoodItemId(rs.getInt("food_item_id"));
        item.setFoodItemName(rs.getString("food_item_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        item.setTotalPrice(rs.getDouble("total_price"));
        return item;
    }
}
