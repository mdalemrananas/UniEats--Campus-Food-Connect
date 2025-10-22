package com.unieats.dao;

import com.unieats.CartItemView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartQueryDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public List<CartItemView> listCartItems(int userId) {
		String sql = """
			SELECT fi.id as item_id, fi.shop_id, fi.name, fi.price, fi.points_multiplier, c.quantity, s.shop_name, fi.stock
			FROM cart c 
			JOIN food_items fi ON c.item_id = fi.id
			JOIN shops s ON fi.shop_id = s.id
			WHERE c.user_id=?
		""";
		List<CartItemView> items = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					items.add(new CartItemView(
						rs.getInt("item_id"),
						rs.getInt("shop_id"),
						rs.getString("name"),
						rs.getDouble("price"),
						rs.getDouble("points_multiplier"),
						rs.getInt("quantity"),
						rs.getString("shop_name"),
						rs.getInt("stock")  // Include stock for real-time display
					));
				}
			}
			return items;
		} catch (SQLException e) { throw new RuntimeException(e); }
	}
}

