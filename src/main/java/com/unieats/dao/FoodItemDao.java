package com.unieats.dao;

import com.unieats.FoodItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDao {
	private static final String DB_URL = "jdbc:sqlite:unieats.db";

	public int create(FoodItem item) {
		String sql = "INSERT INTO food_items(shop_id,name,price,points_multiplier,stock,created_at,updated_at) VALUES(?,?,?,?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, item.getShopId());
			ps.setString(2, item.getName());
			ps.setDouble(3, item.getPrice());
			ps.setDouble(4, item.getPointsMultiplier());
			ps.setInt(5, item.getStock());
			String now = LocalDateTime.now().toString();
			ps.setString(6, now);
			ps.setString(7, now);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) return rs.getInt(1);
			}
			return -1;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to create food item", e);
		}
	}

	public void update(FoodItem item) {
		String sql = "UPDATE food_items SET name=?, price=?, points_multiplier=?, stock=?, updated_at=? WHERE id=?";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, item.getName());
			ps.setDouble(2, item.getPrice());
			ps.setDouble(3, item.getPointsMultiplier());
			ps.setInt(4, item.getStock());
			ps.setString(5, LocalDateTime.now().toString());
			ps.setInt(6, item.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update food item", e);
		}
	}

	public void delete(int id) {
		String sql = "DELETE FROM food_items WHERE id=?";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to delete food item", e);
		}
	}

	public List<FoodItem> listByShop(int shopId) {
		String sql = "SELECT * FROM food_items WHERE shop_id=? ORDER BY updated_at DESC";
		List<FoodItem> list = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, shopId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) list.add(map(rs));
			}
			return list;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to list food items", e);
		}
	}

	public FoodItem getById(int id) {
		String sql = "SELECT * FROM food_items WHERE id=?";
		try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return map(rs);
				return null;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch food item by id", e);
		}
	}

	private FoodItem map(ResultSet rs) throws SQLException {
		FoodItem fi = new FoodItem(
			rs.getInt("shop_id"),
			rs.getString("name"),
			rs.getDouble("price"),
			rs.getDouble("points_multiplier"),
			rs.getInt("stock")
		);
		fi.setId(rs.getInt("id"));
		try { fi.setDescription(rs.getString("description")); } catch (SQLException ignored) {}
		try { fi.setImages(rs.getString("images")); } catch (SQLException ignored) {}
		try { fi.setDiscount(rs.getObject("discount") != null ? rs.getDouble("discount") : null); } catch (SQLException ignored) {}
		try {
			fi.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
			fi.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
		} catch (Exception ignored) {}
		return fi;
	}
    
    public List<FoodItem> getRandomItems(int limit) {
        String sql = "SELECT * FROM food_items ORDER BY RANDOM() LIMIT ?";
        List<FoodItem> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(map(rs));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch random food items", e);
        }
    }
    
    /**
     * Update stock quantity for a food item by reducing the ordered quantity
     */
    public void updateStock(int itemId, int quantityToReduce) {
        String sql = "UPDATE food_items SET stock = stock - ?, updated_at = ? WHERE id = ? AND stock >= ?";
        try (Connection conn = DriverManager.getConnection(DB_URL); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityToReduce);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setInt(3, itemId);
            ps.setInt(4, quantityToReduce); // Ensure we have enough stock
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                // Check if item exists and has enough stock
                FoodItem item = getById(itemId);
                if (item == null) {
                    throw new RuntimeException("Food item with ID " + itemId + " not found");
                } else if (item.getStock() < quantityToReduce) {
                    throw new RuntimeException("Insufficient stock for item " + item.getName() + ". Available: " + item.getStock() + ", Required: " + quantityToReduce);
                } else {
                    throw new RuntimeException("Failed to update stock for food item " + itemId);
                }
            }
            System.out.println("Stock updated for item " + itemId + ": reduced by " + quantityToReduce);
        } catch (SQLException e) {
            System.err.println("SQL Error updating stock for item " + itemId + ": " + e.getMessage());
            throw new RuntimeException("Failed to update stock for food item " + itemId, e);
        }
    }
    
    /**
     * Search food items by name or shop name
     */
    public List<FoodItem> searchItems(String searchTerm) {
        String sql = "SELECT fi.* FROM food_items fi " +
                    "JOIN shops s ON fi.shop_id = s.id " +
                    "WHERE LOWER(fi.name) LIKE LOWER(?) OR LOWER(s.shop_name) LIKE LOWER(?) " +
                    "ORDER BY fi.updated_at DESC";
        List<FoodItem> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(map(rs));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search food items", e);
        }
    }
}
