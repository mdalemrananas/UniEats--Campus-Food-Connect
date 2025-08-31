package com.unieats.dao;

import java.sql.*;

public class OrderDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public int createOrder(int userId, int shopId, double totalPrice, String status) {
		String sql = "INSERT INTO orders(user_id,shop_id,total_price,status) VALUES(?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, userId);
			ps.setInt(2, shopId);
			ps.setDouble(3, totalPrice);
			ps.setString(4, status);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
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
}

