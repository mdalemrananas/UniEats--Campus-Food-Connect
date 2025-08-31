package com.unieats.dao;

import com.unieats.Shop;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShopDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public int createShop(Shop shop) {
		String sql = "INSERT INTO shops(owner_id, shop_name, status, created_at, updated_at) VALUES(?,?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, shop.getOwnerId());
			ps.setString(2, shop.getShopName());
			ps.setString(3, shop.getStatus());
			String now = LocalDateTime.now().toString();
			ps.setString(4, now);
			ps.setString(5, now);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) return rs.getInt(1);
			}
			return -1;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to create shop", e);
		}
	}

	public void updateStatus(int shopId, String status) {
		String sql = "UPDATE shops SET status=?, updated_at=? WHERE id=?";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, status);
			ps.setString(2, LocalDateTime.now().toString());
			ps.setInt(3, shopId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update shop status", e);
		}
	}

	public List<Shop> getApprovedShops() {
		return listByStatus("approved");
	}

	public List<Shop> getPendingShops() {
		return listByStatus("pending");
	}

	private List<Shop> listByStatus(String status) {
		String sql = "SELECT * FROM shops WHERE status=? ORDER BY created_at DESC";
		List<Shop> shops = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, status);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) shops.add(map(rs));
			}
			return shops;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to list shops", e);
		}
	}

	public Shop findById(int id) {
		String sql = "SELECT * FROM shops WHERE id=?";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return map(rs);
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to find shop", e);
		}
	}

	private Shop map(ResultSet rs) throws SQLException {
		Shop s = new Shop(rs.getInt("owner_id"), rs.getString("shop_name"), rs.getString("status"));
		s.setId(rs.getInt("id"));
		try {
			s.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
			s.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
		} catch (Exception ignored) {}
		return s;
	}
}

