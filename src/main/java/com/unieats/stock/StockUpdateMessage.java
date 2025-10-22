package com.unieats.stock;

/**
 * Message object for WebSocket stock updates.
 * This is sent to all connected clients when stock changes.
 */
public class StockUpdateMessage {
    private int itemId;
    private String itemName;
    private int newStock;
    private String timestamp;
    
    public StockUpdateMessage() {
    }
    
    public StockUpdateMessage(int itemId, String itemName, int newStock, String timestamp) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.newStock = newStock;
        this.timestamp = timestamp;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public int getNewStock() {
        return newStock;
    }
    
    public void setNewStock(int newStock) {
        this.newStock = newStock;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Convert to JSON string manually (simple approach without external library)
     */
    public String toJson() {
        return String.format(
            "{\"type\":\"STOCK_UPDATE\",\"itemId\":%d,\"itemName\":\"%s\",\"newStock\":%d,\"timestamp\":\"%s\"}",
            itemId, 
            itemName.replace("\"", "\\\""), // Escape quotes
            newStock, 
            timestamp
        );
    }
    
    /**
     * Parse JSON string to StockUpdateMessage (simple approach)
     */
    public static StockUpdateMessage fromJson(String json) {
        try {
            StockUpdateMessage msg = new StockUpdateMessage();
            
            // Extract itemId
            int itemIdStart = json.indexOf("\"itemId\":") + 9;
            int itemIdEnd = json.indexOf(",", itemIdStart);
            msg.itemId = Integer.parseInt(json.substring(itemIdStart, itemIdEnd).trim());
            
            // Extract itemName
            int itemNameStart = json.indexOf("\"itemName\":\"") + 12;
            int itemNameEnd = json.indexOf("\"", itemNameStart);
            msg.itemName = json.substring(itemNameStart, itemNameEnd);
            
            // Extract newStock
            int newStockStart = json.indexOf("\"newStock\":") + 11;
            int newStockEnd = json.indexOf(",", newStockStart);
            if (newStockEnd == -1) {
                newStockEnd = json.indexOf("}", newStockStart);
            }
            msg.newStock = Integer.parseInt(json.substring(newStockStart, newStockEnd).trim());
            
            // Extract timestamp
            int timestampStart = json.indexOf("\"timestamp\":\"") + 13;
            int timestampEnd = json.indexOf("\"", timestampStart);
            msg.timestamp = json.substring(timestampStart, timestampEnd);
            
            return msg;
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }
}
