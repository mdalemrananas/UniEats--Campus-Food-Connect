package com.unieats.dao;

import com.unieats.OrderInfo;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public int createOrder(int userId, int shopId, double totalPrice, String status) {
		String sql = "INSERT INTO orders(user_id,shop_id,total_price,status) VALUES(?,?,?,?)";
		String selectSql = "SELECT last_insert_rowid()";
		try (Connection conn = DriverManager.getConnection(DB_URL)) {
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, userId);
				ps.setInt(2, shopId);
				ps.setDouble(3, totalPrice);
				ps.setString(4, status);
				ps.executeUpdate();
			}
			try (PreparedStatement ps = conn.prepareStatement(selectSql);
				 ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getInt(1);
			}
			return -1;
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public void addOrderItem(int orderId, int itemId, int quantity, double price) {
		String sql = "INSERT INTO order_items(order_id,item_id,quantity,price) VALUES(?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, orderId);
			ps.setInt(2, itemId);
			ps.setInt(3, quantity);
			ps.setDouble(4, price);
			ps.executeUpdate();
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	/**
	 * Get order details by order ID
	 */
	public OrderInfo getOrderById(int orderId) {
		String sql = """
			SELECT o.*, s.shop_name 
			FROM orders o 
			JOIN shops s ON o.shop_id = s.id 
			WHERE o.id = ?
		""";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, orderId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					OrderInfo order = new OrderInfo(
						rs.getInt("id"),
						rs.getInt("user_id"),
						rs.getInt("shop_id"),
						rs.getString("shop_name"),
						rs.getDouble("total_price"),
						rs.getString("status"),
						parseDateTime(rs.getString("created_at")),
						parseDateTime(rs.getString("updated_at"))
					);
					
					// Load order items
					order.setItems(getOrderItems(orderId));
					
					// Load payment info
					PaymentDao paymentDao = new PaymentDao();
					order.setPayment(paymentDao.getPaymentByOrderId(orderId));
					
					return order;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get order items for a specific order
	 */
	public List<OrderInfo.OrderItemInfo> getOrderItems(int orderId) {
		String sql = """
			SELECT oi.item_id, fi.name as item_name, oi.quantity, oi.price 
			FROM order_items oi 
			JOIN food_items fi ON oi.item_id = fi.id 
			WHERE oi.order_id = ?
		""";
		List<OrderInfo.OrderItemInfo> items = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, orderId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					items.add(new OrderInfo.OrderItemInfo(
						rs.getInt("item_id"),
						rs.getString("item_name"),
						rs.getInt("quantity"),
						rs.getDouble("price")
					));
				}
			}
			return items;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Update order status
	 */
	public void updateOrderStatus(int orderId, String status) {
		String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, status);
			ps.setInt(2, orderId);
			ps.executeUpdate();
			
			// Add to status history
			addStatusHistory(orderId, status, null);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add status history entry
	 */
	private void addStatusHistory(int orderId, String status, String notes) {
		String sql = "INSERT INTO order_status_history(order_id, status, notes) VALUES(?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, orderId);
			ps.setString(2, status);
			ps.setString(3, notes);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get orders for a specific user
	 */
	public List<OrderInfo> getOrdersByUserId(int userId) {
		String sql = """
			SELECT o.*, s.shop_name 
			FROM orders o 
			JOIN shops s ON o.shop_id = s.id 
			WHERE o.user_id = ? 
			ORDER BY o.created_at DESC
		""";
		List<OrderInfo> orders = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OrderInfo order = new OrderInfo(
						rs.getInt("id"),
						rs.getInt("user_id"),
						rs.getInt("shop_id"),
						rs.getString("shop_name"),
						rs.getDouble("total_price"),
						rs.getString("status"),
						parseDateTime(rs.getString("created_at")),
						parseDateTime(rs.getString("updated_at"))
					);
					orders.add(order);
				}
			}
			return orders;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get current/active orders for a specific user (pending, preparing, ready, out_for_delivery)
	 */
	public List<OrderInfo> getCurrentOrdersByUserId(int userId) {
		String sql = """
			SELECT o.*, s.shop_name 
			FROM orders o 
			JOIN shops s ON o.shop_id = s.id 
			WHERE o.user_id = ? 
			AND o.status IN ('pending', 'preparing', 'ready', 'out_for_delivery')
			ORDER BY o.created_at DESC
		""";
		List<OrderInfo> orders = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OrderInfo order = new OrderInfo(
						rs.getInt("id"),
						rs.getInt("user_id"),
						rs.getInt("shop_id"),
						rs.getString("shop_name"),
						rs.getDouble("total_price"),
						rs.getString("status"),
						parseDateTime(rs.getString("created_at")),
						parseDateTime(rs.getString("updated_at"))
					);
					orders.add(order);
				}
			}
			return orders;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get order history for a specific user (delivered, cancelled)
	 */
	public List<OrderInfo> getOrderHistoryByUserId(int userId) {
		String sql = """
			SELECT o.*, s.shop_name 
			FROM orders o 
			JOIN shops s ON o.shop_id = s.id 
			WHERE o.user_id = ? 
			AND o.status IN ('delivered', 'cancelled')
			ORDER BY o.created_at DESC
		""";
		List<OrderInfo> orders = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OrderInfo order = new OrderInfo(
						rs.getInt("id"),
						rs.getInt("user_id"),
						rs.getInt("shop_id"),
						rs.getString("shop_name"),
						rs.getDouble("total_price"),
						rs.getString("status"),
						parseDateTime(rs.getString("created_at")),
						parseDateTime(rs.getString("updated_at"))
					);
					orders.add(order);
				}
			}
			return orders;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private LocalDateTime parseDateTime(String dateTimeStr) {
		if (dateTimeStr == null) return null;
		try {
			return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		} catch (Exception e) {
			return LocalDateTime.now();
		}
	}
}

