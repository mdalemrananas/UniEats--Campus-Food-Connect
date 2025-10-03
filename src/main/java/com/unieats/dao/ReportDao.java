package com.unieats.dao;

import java.sql.*;

public class ReportDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public int submitReport(int userId, int shopId, Integer itemId, String title, String description, String attachmentsJson) {
		String sql = "INSERT INTO reports(user_id, shop_id, item_id, title, description, attachments) VALUES(?,?,?,?,?,?)";
		String selectSql = "SELECT last_insert_rowid()";
		try (Connection conn = DriverManager.getConnection(DB_URL)) {
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, userId);
				ps.setInt(2, shopId);
				if (itemId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, itemId);
				ps.setString(4, title);
				ps.setString(5, description);
				ps.setString(6, attachmentsJson == null ? "[]" : attachmentsJson);
				ps.executeUpdate();
			}
			try (PreparedStatement ps = conn.prepareStatement(selectSql);
				 ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getInt(1);
			}
			return -1;
		} catch (SQLException e) { throw new RuntimeException(e); }
	}
    public ResultSet listAll(Connection external) throws SQLException {
        String sql = "SELECT r.*, u.full_name as user_name, s.shop_name as shop_name FROM reports r JOIN users u ON r.user_id=u.id JOIN shops s ON r.shop_id=s.id ORDER BY r.created_at DESC";
        PreparedStatement ps = external.prepareStatement(sql);
        return ps.executeQuery();
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM reports WHERE status='open' OR status='reviewing'";
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /**
     * Get total count of all reports
     */
    public int getTotalReportsCount() {
        String sql = "SELECT COUNT(*) FROM reports";
        try (Connection conn = DriverManager.getConnection(DB_URL); 
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) { 
            System.err.println("Error getting total reports count: " + e.getMessage());
            return 0; 
        }
    }
}