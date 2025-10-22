package com.unieats;

public class CartItemView {
	public final int itemId;
	public final int shopId;
	public final String name;
	public final double price;
	public final double pointsMultiplier;
	public final int quantity;
	public final String shopName;
	public final int stock; // Added stock field for real-time stock display

	public CartItemView(int itemId, int shopId, String name, double price, double pointsMultiplier, int quantity, String shopName, int stock) {
		this.itemId = itemId;
		this.shopId = shopId;
		this.name = name;
		this.price = price;
		this.pointsMultiplier = pointsMultiplier;
		this.quantity = quantity;
		this.shopName = shopName;
		this.stock = stock;
	}
	
	// Backward compatibility constructor
	public CartItemView(int itemId, int shopId, String name, double price, double pointsMultiplier, int quantity, String shopName) {
		this(itemId, shopId, name, price, pointsMultiplier, quantity, shopName, 0);
	}
}

