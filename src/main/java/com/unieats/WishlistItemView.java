package com.unieats;

public class WishlistItemView {
    private int itemId;
    private int shopId;
    private String itemName;
    private double price;
    private String shopName;
    private String description;
    private String images;

    public WishlistItemView(int itemId, int shopId, String itemName, double price, String shopName, String description, String images) {
        this.itemId = itemId;
        this.shopId = shopId;
        this.itemName = itemName;
        this.price = price;
        this.shopName = shopName;
        this.description = description;
        this.images = images;
    }

    // Getters
    public int getItemId() { return itemId; }
    public int getShopId() { return shopId; }
    public String getItemName() { return itemName; }
    public double getPrice() { return price; }
    public String getShopName() { return shopName; }
    public String getDescription() { return description; }
    public String getImages() { return images; }

    // Setters
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setPrice(double price) { this.price = price; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setDescription(String description) { this.description = description; }
    public void setImages(String images) { this.images = images; }
}
