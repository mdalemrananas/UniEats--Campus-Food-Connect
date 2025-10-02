package com.unieats.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public record Review(int id, int userId, String userName, Integer shopId, Integer foodItemId, int rating, String comment, String createdAt) {}

	public void addShopReview(int userId, int shopId, int rating, String comment) {
		String sql = "INSERT INTO reviews(user_id, shop_id, rating, comment) VALUES(?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setInt(2, shopId);
			ps.setInt(3, rating);
			ps.setString(4, comment);
			ps.executeUpdate();
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public void addFoodReview(int userId, int foodItemId, int rating, String comment) {
		String sql = "INSERT INTO reviews(user_id, food_item_id, rating, comment) VALUES(?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setInt(2, foodItemId);
			ps.setInt(3, rating);
			ps.setString(4, comment);
			ps.executeUpdate();
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public double getAverageRatingForShop(int shopId) {
		String sql = "SELECT AVG(rating) FROM reviews WHERE shop_id = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, shopId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getDouble(1) : 0.0;
			}
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public double getAverageRatingForFood(int foodItemId) {
		String sql = "SELECT AVG(rating) FROM reviews WHERE food_item_id = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, foodItemId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getDouble(1) : 0.0;
			}
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public List<Review> listReviewsForShop(int shopId, int limit) {
		String sql = "SELECT r.*, u.full_name as user_name FROM reviews r JOIN users u ON u.id = r.user_id WHERE r.shop_id = ? ORDER BY r.created_at DESC LIMIT ?";
		List<Review> out = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, shopId);
			ps.setInt(2, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) out.add(map(rs));
			}
			return out;
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	public List<Review> listReviewsForFood(int foodItemId, int limit) {
		String sql = "SELECT r.*, u.full_name as user_name FROM reviews r JOIN users u ON u.id = r.user_id WHERE r.food_item_id = ? ORDER BY r.created_at DESC LIMIT ?";
		List<Review> out = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, foodItemId);
			ps.setInt(2, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) out.add(map(rs));
			}
			return out;
		} catch (SQLException e) { throw new RuntimeException(e); }
	}

	private Review map(ResultSet rs) throws SQLException {
		return new Review(
			rs.getInt("id"),
			rs.getInt("user_id"),
			rs.getString("user_name"),
			(rs.getObject("shop_id") == null ? null : rs.getInt("shop_id")),
			(rs.getObject("food_item_id") == null ? null : rs.getInt("food_item_id")),
			rs.getInt("rating"),
			rs.getString("comment"),
			rs.getString("created_at")
		);
	}
}
