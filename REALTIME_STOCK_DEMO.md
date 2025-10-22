# Real-Time Stock Update Demo 🚀

This demo showcases **real-time, bi-directional stock updates** between multiple JavaFX clients using WebSocket technology.

## 📋 Scenario

**Situation:** Two users (A and B) open a JavaFX client connected to the same server.

- **Initial State:** A burger item has **stock = 1**
- **Action:** User A clicks "Buy Now" 
- **Result:** Stock becomes **0**
- **Real-Time Update:** User B's interface **immediately** updates to show "Out of Stock" — **NO polling or refreshing!**

---

## 🏗️ Architecture

### Backend Components

#### 1. **StockService.java** (`com.unieats.stock`)
- Thread-safe stock management service
- Uses `ReentrantLock` to prevent race conditions
- Ensures only one user can purchase when stock = 1
- Handles concurrent access safely

#### 2. **StockWebSocketServer.java** (`com.unieats.stock`)
- WebSocket server running on port **8080**
- Handles client connections
- Processes purchase requests
- **Broadcasts stock updates to ALL connected clients instantly**

#### 3. **StockUpdateMessage.java** (`com.unieats.stock`)
- Message format for WebSocket communication
- Simple JSON serialization/deserialization
- Contains: itemId, itemName, newStock, timestamp

#### 4. **RealTimeStockServer.java** (`com.unieats.demo`)
- Standalone server application
- Initializes database
- Creates demo burger item with stock = 1
- Starts WebSocket server

### Frontend Components

#### 1. **StockWebSocketClient.java** (`com.unieats.websocket`)
- JavaFX-compatible WebSocket client
- Connects to server
- Receives real-time stock updates
- **Uses `Platform.runLater()` for thread-safe UI updates**

#### 2. **RealTimeStockDemoClient.java** (`com.unieats.demo`)
- Beautiful JavaFX UI
- Shows real-time stock count
- "Buy Now" button to place orders
- Activity log for tracking events
- Connection status indicator

---

## 🚀 How to Run

### Method 1: Using PowerShell Script (Recommended)

#### Step 1: Start the Server
```powershell
.\run-demo.ps1 server
```

Wait until you see:
```
✓ Server is running!
==============================================================
  WebSocket Port: 8080
  Demo Item ID: 1
  Demo Item: Demo Burger
  Initial Stock: 1
==============================================================
```

#### Step 2: Start First Client (User A)
Open a **new terminal** and run:
```powershell
.\run-demo.ps1 client
```

#### Step 3: Start Second Client (User B)
Open **another new terminal** and run:
```powershell
.\run-demo.ps1 client
```

#### Step 4: Test Real-Time Updates
1. Both clients should show **"Stock: 1"** in green
2. Click **"Buy Now"** in Client A
3. **Watch both clients update instantly!**
   - Client A shows: Stock: 0 (Out of Stock)
   - Client B shows: Stock: 0 (Out of Stock) — **WITHOUT any refresh!**

---

### Method 2: Using Maven Commands

#### Terminal 1 - Start Server:
```powershell
mvn clean compile
mvn exec:java -Dexec.mainClass="com.unieats.demo.RealTimeStockServer"
```

#### Terminal 2 - Start Client A:
```powershell
mvn javafx:run -Djavafx.mainClass="com.unieats.demo.RealTimeStockDemoClient"
```

#### Terminal 3 - Start Client B:
```powershell
mvn javafx:run -Djavafx.mainClass="com.unieats.demo.RealTimeStockDemoClient"
```

---

## 🔍 What's Happening Behind the Scenes?

### When User A Clicks "Buy Now":

1. **Client A** sends WebSocket message to server:
   ```json
   {"type":"PURCHASE","itemId":1}
   ```

2. **Server** receives the request:
   - Acquires lock (thread-safe)
   - Checks current stock in database
   - If stock > 0: decreases stock by 1
   - Releases lock

3. **Server** broadcasts to **ALL clients** (including A):
   ```json
   {
     "type":"STOCK_UPDATE",
     "itemId":1,
     "itemName":"Demo Burger",
     "newStock":0,
     "timestamp":"2025-10-14T23:30:45"
   }
   ```

4. **All Clients** (A and B) receive the update:
   - WebSocket client receives message on background thread
   - Calls `Platform.runLater()` to update UI safely
   - UI updates immediately: Stock label changes to "0"
   - Status changes to "Out of Stock" (red)
   - Buy button becomes disabled

### Key Features Demonstrated:

✅ **No Polling** - Pure push-based updates via WebSocket  
✅ **Thread-Safe** - Concurrent purchase attempts handled safely  
✅ **Real-Time** - Updates appear instantly (< 100ms typically)  
✅ **Bi-Directional** - Clients can send requests and receive broadcasts  
✅ **Multi-Client** - Any number of clients can connect  
✅ **UI Thread Safety** - Uses `Platform.runLater()` for JavaFX updates  

---

## 🛠️ Technical Details

### Concurrency Safety

The `StockService` uses a `ReentrantLock` to prevent race conditions:

```java
stockLock.lock();
try {
    // Check stock
    if (item.getStock() > 0) {
        item.setStock(item.getStock() - 1);
        dao.update(item);
        return true;
    }
    return false;
} finally {
    stockLock.unlock();
}
```

**Why this works:**
- If two users try to buy simultaneously when stock = 1
- One acquires the lock first and decreases stock to 0
- The second user's request finds stock = 0 and fails
- Only ONE purchase succeeds (as expected)

### WebSocket Communication

**Message Types:**

1. **PURCHASE** (Client → Server)
   ```json
   {"type":"PURCHASE","itemId":1}
   ```

2. **GET_STOCK** (Client → Server)
   ```json
   {"type":"GET_STOCK","itemId":1}
   ```

3. **STOCK_UPDATE** (Server → All Clients)
   ```json
   {
     "type":"STOCK_UPDATE",
     "itemId":1,
     "itemName":"Demo Burger",
     "newStock":0,
     "timestamp":"2025-10-14T23:30:45"
   }
   ```

4. **PURCHASE_FAILED** (Server → Requesting Client)
   ```json
   {"type":"PURCHASE_FAILED","itemId":1,"message":"Out of stock"}
   ```

### Thread Safety in JavaFX

JavaFX UI components must be updated on the **JavaFX Application Thread**. The WebSocket client receives messages on a background thread, so we use:

```java
Platform.runLater(() -> {
    // Update UI components here
    stockLabel.setText("Stock: " + newStock);
});
```

This ensures thread-safe UI updates without blocking the WebSocket thread.

---

## 🧪 Testing Scenarios

### Scenario 1: Basic Real-Time Update
1. Start server and 2 clients
2. Both show stock = 1
3. Client A buys → Both show stock = 0 instantly

### Scenario 2: Concurrent Purchase Attempt
1. Start server and 2 clients
2. Both show stock = 1
3. Click "Buy Now" in **both clients at the same time**
4. Only ONE purchase succeeds
5. Both clients show stock = 0

### Scenario 3: Late Joiner
1. Start server and Client A
2. Client A buys (stock becomes 0)
3. Start Client B (connects after purchase)
4. Client B immediately sees stock = 0

### Scenario 4: Multiple Items (Extension)
1. Modify server to create multiple items
2. Clients can buy different items
3. Stock updates only affect specific items

---

## 📦 Dependencies

All dependencies are already in `pom.xml`:

- **JavaFX** 21.0.2 (UI framework)
- **Java-WebSocket** 1.5.6 (WebSocket library)
- **SQLite JDBC** 3.44.1.0 (Database)
- **Java** 17+ (Required)

---

## 🔧 Troubleshooting

### Problem: "Connection refused"
- **Solution:** Make sure the server is running first
- Check server console for "Server is running!" message
- Verify port 8080 is not blocked by firewall

### Problem: Clients don't update
- **Solution:** Check server console for "New client connected" messages
- Verify WebSocket messages are being sent (check server logs)
- Try restarting both server and clients

### Problem: "Out of memory" or JavaFX issues
- **Solution:** Ensure Java 17+ is installed
- Check JavaFX modules are properly loaded
- Try running with: `mvn clean compile` first

### Problem: Stock doesn't decrease
- **Solution:** Check database permissions
- Verify `unieats.db` file exists
- Check server logs for SQL errors

---

## 🎯 Key Takeaways

1. **WebSocket enables true real-time communication** - No need for polling
2. **Thread safety is critical** - Use locks for concurrent access
3. **Platform.runLater() is essential** - For JavaFX UI updates from background threads
4. **Broadcast pattern works well** - Server broadcasts to all clients simultaneously
5. **Simple JSON messaging** - No heavy frameworks needed for basic communication

---

## 📝 Code Structure

```
src/main/java/com/unieats/
├── stock/
│   ├── StockService.java              # Thread-safe stock management
│   ├── StockUpdateMessage.java        # Message format
│   └── StockWebSocketServer.java      # WebSocket server
├── websocket/
│   └── StockWebSocketClient.java      # JavaFX WebSocket client
└── demo/
    ├── RealTimeStockServer.java       # Server application
    └── RealTimeStockDemoClient.java   # Client application
```

---

## 🚀 Next Steps / Extensions

Want to extend this demo? Try:

1. **Add multiple items** - Track stock for multiple burgers
2. **User authentication** - Show which user made the purchase
3. **Purchase history** - Display recent orders
4. **Stock replenishment** - Button to add stock back
5. **Price changes** - Broadcast price updates too
6. **Order queue** - Reserve items for X seconds before purchase
7. **Analytics dashboard** - Show real-time purchase statistics

---

## 📄 License

Part of the UniEats project.

---

## 🙋 Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review server console output
3. Verify all dependencies are installed
4. Ensure Java 17+ and Maven are properly configured

**Happy Testing! 🎉**
