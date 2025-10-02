package com.unieats;

import java.sql.*;

public class RewardService {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public static void addPoints(int userId, int shopId, double points) {
		String upsert = """
			INSERT INTO reward_points(user_id, shop_id, points) VALUES(?, ?, ?)
			ON CONFLICT(user_id, shop_id) DO UPDATE SET points = points + excluded.points
		""";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(upsert)) {
			ps.setInt(1, userId);
			ps.setInt(2, shopId);
			ps.setDouble(3, points);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to add points", e);
		}
	}

	public static double getPoints(int userId, int shopId) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement("SELECT points FROM reward_points WHERE user_id=? AND shop_id=?")) {
			ps.setInt(1, userId);
			ps.setInt(2, shopId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getDouble(1);
				return 0.0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to get points", e);
		}
	}

	public static boolean redeemPoints(int userId, int shopId, double pointsToUse) {
		String select = "SELECT points FROM reward_points WHERE user_id=? AND shop_id=?";
		String update = "UPDATE reward_points SET points = points - ? WHERE user_id=? AND shop_id=?";
		try (Connection conn = DriverManager.getConnection(DB_URL)) {
			conn.setAutoCommit(false);
			try (PreparedStatement ps = conn.prepareStatement(select)) {
				ps.setInt(1, userId);
				ps.setInt(2, shopId);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next() || rs.getDouble(1) < pointsToUse) {
						conn.rollback();
						return false;
					}
				}
			}
			try (PreparedStatement ps2 = conn.prepareStatement(update)) {
				ps2.setDouble(1, pointsToUse);
				ps2.setInt(2, userId);
				ps2.setInt(3, shopId);
				ps2.executeUpdate();
			}
			conn.commit();
			return true;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to redeem points", e);
		}
	}

	/**
	 * Get total reward points across all shops for a user.
	 */
	public static double getTotalPoints(int userId) {
		String sql = "SELECT COALESCE(SUM(points),0) FROM reward_points WHERE user_id=?";
		try (Connection conn = DriverManager.getConnection(DB_URL);
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getDouble(1);
				return 0.0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to get total points", e);
		}
	}
}

