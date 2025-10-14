package com.unieats;

public class OrderItem {
    private int id;
    private int foodItemId;
    private String foodItemName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    
    public OrderItem() {}
    
    public OrderItem(int foodItemId, String foodItemName, int quantity, double unitPrice) {
        this.foodItemId = foodItemId;
        this.foodItemName = foodItemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getFoodItemId() { return foodItemId; }
    public void setFoodItemId(int foodItemId) { this.foodItemId = foodItemId; }
    
    public String getFoodItemName() { return foodItemName; }
    public void setFoodItemName(String foodItemName) { this.foodItemName = foodItemName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        this.totalPrice = this.quantity * this.unitPrice;
    }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice; 
        this.totalPrice = this.quantity * this.unitPrice;
    }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "foodItemName='" + foodItemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
