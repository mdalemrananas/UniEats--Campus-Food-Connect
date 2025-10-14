package com.unieats.dao;

import com.unieats.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WishlistDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public void addToWishlist(int userId, int itemId, int quantity) {
		String sql = """
			INSERT INTO wishlist(user_id, item_id, quantity)
			VALUES (?, ?, ?)
		""";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setInt(2, itemId);
			ps.setInt(3, Math.max(1, quantity));
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeFromWishlist(int userId, int itemId) {
		String sql = "DELETE FROM wishlist WHERE user_id = ? AND item_id = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setInt(2, itemId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isInWishlist(int userId, int itemId) {
		String sql = "SELECT 1 FROM wishlist WHERE user_id = ? AND item_id = ? LIMIT 1";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setInt(2, itemId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public Set<Integer> getWishlistItemIds(int userId) {
		String sql = "SELECT item_id FROM wishlist WHERE user_id = ?";
		Set<Integer> ids = new HashSet<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) ids.add(rs.getInt("item_id"));
			}
			return ids;
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public List<FoodItem> listWishlistItems(int userId) {
		String sql = """
			SELECT fi.*
			FROM wishlist w
			JOIN food_items fi ON w.item_id = fi.id
			WHERE w.user_id = ?
			ORDER BY w.created_at DESC
		""";
		List<FoodItem> items = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					FoodItem item = new FoodItem(
						rs.getInt("shop_id"),
						rs.getString("name"),
						rs.getDouble("price"),
						rs.getDouble("points_multiplier"),
						rs.getInt("stock")
					);
					item.setId(rs.getInt("id"));
					try { item.setDescription(rs.getString("description")); } catch (SQLException ignored) {}
					try { item.setImages(rs.getString("images")); } catch (SQLException ignored) {}
					try { item.setDiscount(rs.getDouble("discount")); } catch (SQLException ignored) {}
					items.add(item);
				}
			}
			return items;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
