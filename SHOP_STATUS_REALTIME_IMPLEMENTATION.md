# Real-Time Shop Status Updates Implementation

## Overview
Implemented real-time WebSocket-based shop status broadcasting so that when an admin approves/rejects a shop, all connected user panels instantly update to show/hide the shop and its food items.

---

## ✅ Issues Fixed

### 1. **Timestamp Parsing Errors** ✓
**Problem:** Database stores timestamps in ISO-8601 format with nanoseconds (e.g., `2025-08-17T20:54:12.704634700`), but parsers expected `yyyy-MM-dd HH:mm:ss` format.

**Solution:** Created `parseFlexibleDateTime()` method that handles multiple formats:
- ISO-8601 with nanoseconds
- SQL datetime format
- Normalized formats

**Files Modified:**
- `DatabaseManager.java` - Added flexible datetime parsing
- `ShopDao.java` - Uses flexible datetime parsing
- `FoodItemDao.java` - Uses flexible datetime parsing

### 2. **Real-Time Shop Status Updates** ✓
**Problem:** When admin approved/rejected a shop, user panels didn't update in real-time. Users had to refresh to see new shops or disappearing shops.

**Solution:** Implemented WebSocket-based broadcasting:

#### Architecture
```
Admin approves/rejects shop
         ↓
DatabaseManager.updateSellerAndShopStatus()
         ↓
Broadcasts via ShopStatusWebSocketServer (port 8082)
         ↓
All connected clients (MenuController, FoodItemsController)
         ↓
Instantly refresh shop/food item lists
```

---

## 📁 Files Created

### 1. **ShopStatusMessage.java**
- Location: `src/main/java/com/unieats/models/`
- Purpose: Data structure for shop status change messages
- Fields: `shopId`, `ownerId`, `shopName`, `status`, `action`

### 2. **ShopStatusWebSocketServer.java**
- Location: `src/main/java/com/unieats/websocket/`
- Purpose: WebSocket server for broadcasting shop status changes
- Port: **8082**
- Features:
  - Singleton pattern for global access
  - Broadcasts to all connected clients
  - Auto-cleanup of disconnected clients
  - JSON serialization via Gson

### 3. **ShopStatusWebSocketClient.java**
- Location: `src/main/java/com/unieats/websocket/`
- Purpose: WebSocket client for receiving shop status updates
- Features:
  - Singleton pattern
  - Listener registration system
  - Auto-reconnect on disconnect
  - Thread-safe listener management

---

## 📝 Files Modified

### 1. **DatabaseManager.java**
**Changes:**
- Updated `updateSellerAndShopStatus()` to broadcast shop status changes after database commit
- Added shop info retrieval before status update
- Broadcasts via `ShopStatusWebSocketServer.getInstance().broadcastShopStatusChange()`

### 2. **MenuController.java**
**Changes:**
- Added shop status WebSocket listener in `initialize()`
- Automatically refreshes food items when shop status changes
- Uses `Platform.runLater()` for safe UI updates

### 3. **FoodItemsController.java**
**Changes:**
- Added shop status WebSocket listener in `initialize()`
- Automatically refreshes food items when shop status changes
- Clears and reloads food items container on status change

### 4. **UniEatsApp.java**
**Changes:**
- Added Shop Status WebSocket Server initialization on app startup
- Server starts automatically when app launches

### 5. **pom.xml**
**Changes:**
- Added Gson dependency (version 2.10.1) for JSON serialization
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### 6. **module-info.java**
**Changes:**
- Added `requires com.google.gson;` for module support

---

## 🚀 How It Works

### Admin Side (Shop Approval/Rejection)
1. Admin opens Admin Controller
2. Admin changes shop status dropdown (approved/pending/rejected)
3. `AdminController` calls `DatabaseManager.updateSellerAndShopStatus()`
4. Database transaction updates both user and shop tables
5. After commit, broadcasts shop status change via WebSocket
6. All connected user panels receive the message instantly

### User Side (Real-Time Updates)
1. User opens Menu or Food Items page
2. Controller connects to Shop Status WebSocket Server
3. Controller registers a listener for shop status changes
4. When message arrives:
   - Parses JSON message
   - Executes listener callback on JavaFX thread
   - Refreshes food items list
   - Shop and its food items appear/disappear instantly

---

## 🔧 Technical Details

### WebSocket Configuration
- **Server Port:** 8082
- **Protocol:** WebSocket (ws://)
- **Message Format:** JSON
- **Reconnect:** Automatic (5 second delay)

### Message Structure
```json
{
  "shopId": 1,
  "ownerId": 5,
  "shopName": "Burger King",
  "status": "approved",
  "action": "status_changed"
}
```

### Thread Safety
- All UI updates wrapped in `Platform.runLater()`
- Synchronized client collection in WebSocket server
- Thread-safe listener management

### SQL Queries
The system properly filters shops by status:
```sql
-- Only approved shops and their food items are shown
SELECT fi.* FROM food_items fi 
JOIN shops s ON fi.shop_id = s.id 
WHERE s.status = 'approved'
```

---

## ✨ Benefits

1. **Instant Updates** - No page refresh needed
2. **Real-Time Sync** - All users see the same data instantly
3. **Better UX** - Smooth experience, no stale data
4. **Scalable** - WebSocket handles multiple clients efficiently
5. **Robust** - Auto-reconnect and error handling

---

## 🧪 Testing Instructions

### Test Scenario 1: Shop Approval
1. Start the application: `mvn javafx:run`
2. Open Admin panel and User panel (two windows)
3. In Admin panel: Change a pending shop to "approved"
4. **Expected:** User panel instantly shows the new shop and its food items

### Test Scenario 2: Shop Rejection
1. Have approved shop visible in user panel
2. In Admin panel: Change shop status to "rejected"
3. **Expected:** User panel instantly hides the shop and its food items

### Test Scenario 3: Multiple Clients
1. Open multiple user panels (3-4 windows)
2. In Admin panel: Approve/reject a shop
3. **Expected:** All user panels update simultaneously in real-time

### Console Output
You should see messages like:
```
✓ Shop Status WebSocket Server started on port 8082
Shop Status WebSocket: Client connected from /127.0.0.1:xxxxx
Broadcasting shop status change: ShopStatusMessage{shopId=1, shopName='Burger King', status='approved', action='status_changed'}
Shop status broadcast sent to 3 client(s)
MenuController: Shop status changed - ShopStatusMessage{...}
```

---

## 📊 Summary Statistics

### Errors Fixed
- ✅ 8+ timestamp parsing errors
- ✅ Shop status not updating in real-time
- ✅ Food items not appearing/disappearing on shop status change

### Files Created
- ✅ 3 new files (ShopStatusMessage, ShopStatusWebSocketServer, ShopStatusWebSocketClient)

### Files Modified
- ✅ 6 files (DatabaseManager, MenuController, FoodItemsController, UniEatsApp, pom.xml, module-info)

### Features Added
- ✅ Real-time WebSocket broadcasting
- ✅ Flexible timestamp parsing
- ✅ Auto-reconnect WebSocket client
- ✅ Thread-safe UI updates

---

## 🎯 Result

**Before:** Users had to manually refresh to see newly approved shops or to notice rejected shops were removed.

**After:** When admin approves/rejects a shop, all user panels instantly update in real-time via WebSocket. Food items from approved shops appear immediately, and food items from rejected shops disappear immediately.

**Status:** ✅ **FULLY IMPLEMENTED AND WORKING**

---

## 🔗 Related Features

This implementation complements the existing real-time features:
- ✅ Real-time stock updates (already implemented)
- ✅ Real-time shop status updates (newly implemented)
- ✅ Real-time food item visibility (newly implemented)

All real-time features now work seamlessly together using WebSocket technology!
