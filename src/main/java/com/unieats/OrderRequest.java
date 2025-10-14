package com.unieats;

import java.time.LocalDateTime;
import java.util.List;

public class OrderRequest {
    private int id;
    private int customerId;
    private String customerName;
    private int shopId;
    private List<OrderItem> items;
    private double totalPrice;
    private String status; // "pending", "accepted", "declined"
    private LocalDateTime orderTime;
    private LocalDateTime updatedAt;
    
    public OrderRequest() {
        this.orderTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "pending";
    }
    
    public OrderRequest(int customerId, String customerName, int shopId, List<OrderItem> items, double totalPrice) {
        this();
        this.customerId = customerId;
        this.customerName = customerName;
        this.shopId = shopId;
        this.items = items;
        this.totalPrice = totalPrice;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "OrderRequest{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", orderTime=" + orderTime +
                '}';
    }
}
