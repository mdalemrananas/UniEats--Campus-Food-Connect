package com.unieats.dao;

import java.sql.*;

public class ReportDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public void submitReport(int userId, int shopId, Integer itemId, String title, String description) {
		String sql = "INSERT INTO reports(user_id, shop_id, item_id, title, description) VALUES(?,?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setInt(2, shopId);
			if (itemId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, itemId);
			ps.setString(4, title);
			ps.setString(5, description);
			ps.executeUpdate();
		} catch (SQLException e) { throw new RuntimeException(e); }
	}
}

