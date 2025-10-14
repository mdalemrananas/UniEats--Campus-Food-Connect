package com.unieats;

public class CartItemView {
	public final int itemId;
	public final int shopId;
	public final String name;
	public final double price;
	public final double pointsMultiplier;
	public final int quantity;

	public CartItemView(int itemId, int shopId, String name, double price, double pointsMultiplier, int quantity) {
		this.itemId = itemId;
		this.shopId = shopId;
		this.name = name;
		this.price = price;
		this.pointsMultiplier = pointsMultiplier;
		this.quantity = quantity;
	}
}

