package com.unieats;

import java.time.LocalDateTime;

public class Shop {
	private int id;
	private int ownerId;
	private String shopName;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Shop(int ownerId, String shopName, String status) {
		this.ownerId = ownerId;
		this.shopName = shopName;
		this.status = status;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public int getOwnerId() { return ownerId; }
	public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

	public String getShopName() { return shopName; }
	public void setShopName(String shopName) { this.shopName = shopName; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

