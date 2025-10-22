package com.unieats.models;

/**
 * Message structure for shop status updates
 */
public class ShopStatusMessage {
    private int shopId;
    private int ownerId;
    private String shopName;
    private String status; // approved, pending, rejected
    private String action; // "status_changed"
    
    public ShopStatusMessage() {}
    
    public ShopStatusMessage(int shopId, int ownerId, String shopName, String status, String action) {
        this.shopId = shopId;
        this.ownerId = ownerId;
        this.shopName = shopName;
        this.status = status;
        this.action = action;
    }
    
    // Getters and setters
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    @Override
    public String toString() {
        return String.format("ShopStatusMessage{shopId=%d, shopName='%s', status='%s', action='%s'}", 
            shopId, shopName, status, action);
    }
}
