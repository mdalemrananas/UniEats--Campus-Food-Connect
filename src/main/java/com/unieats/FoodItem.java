package com.unieats;

import java.time.LocalDateTime;

public class FoodItem {
	private int id;
	private int shopId;
	private String name;
	private double price;
	private double pointsMultiplier;
	private int stock;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public FoodItem(int shopId, String name, double price, double pointsMultiplier, int stock) {
		this.shopId = shopId;
		this.name = name;
		this.price = price;
		this.pointsMultiplier = pointsMultiplier;
		this.stock = stock;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getShopId() { return shopId; }
	public void setShopId(int shopId) { this.shopId = shopId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public double getPrice() { return price; }
	public void setPrice(double price) { this.price = price; }
	public double getPointsMultiplier() { return pointsMultiplier; }
	public void setPointsMultiplier(double pointsMultiplier) { this.pointsMultiplier = pointsMultiplier; }
	public int getStock() { return stock; }
	public void setStock(int stock) { this.stock = stock; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

