package com.unieats;

import com.unieats.dao.PaymentDao;
import java.time.LocalDateTime;
import java.util.List;

public class OrderInfo {
    private int id;
    private int userId;
    private int shopId;
    private String shopName;
    private double totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemInfo> items;
    private PaymentDao.PaymentInfo payment;

    public OrderInfo(int id, int userId, int shopId, String shopName, double totalPrice, 
                    String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.shopId = shopId;
        this.shopName = shopName;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<OrderItemInfo> getItems() { return items; }
    public void setItems(List<OrderItemInfo> items) { this.items = items; }
    
    public PaymentDao.PaymentInfo getPayment() { return payment; }
    public void setPayment(PaymentDao.PaymentInfo payment) { this.payment = payment; }

    public static class OrderItemInfo {
        public final int itemId;
        public final String itemName;
        public final int quantity;
        public final double price;
        public final double totalPrice;

        public OrderItemInfo(int itemId, String itemName, int quantity, double price) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
            this.totalPrice = price * quantity;
        }
    }
}
