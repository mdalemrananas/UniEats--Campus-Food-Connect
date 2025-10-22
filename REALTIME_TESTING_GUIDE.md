# Real-Time Shop Status Testing Guide

## 🎯 System Overview

**Goal:** When admin approves/rejects a shop, user panel updates **INSTANTLY** without refresh.

**Components:**
- **Admin Panel** - Changes shop status (approved/pending/rejected)
- **WebSocket Server** - Port 8082, broadcasts status changes
- **User Panel** - 3 controllers listen for updates:
  1. **ShopsController** - Shops list page
  2. **MenuController** - Random food items (from approved shops)
  3. **FoodItemsController** - All food items (from approved shops)

---

## 🚀 Step-by-Step Testing Instructions

### **STEP 1: Start Application**

```powershell
mvn clean compile javafx:run
```

### **STEP 2: Verify Server Startup**

**Look for in console:**
```
Creating Shop Status WebSocket Server on port 8082...
✓ Shop Status WebSocket Server started on port 8082
Shop Status WebSocket Server started successfully on port 8082
```

✅ **If you see this** → Server is running correctly  
❌ **If you don't see this** → Server failed to start (check port 8082 isn't in use)

---

### **STEP 3: Login and Navigate to Shops Page**

1. Login as a **regular user** (not admin, not seller)
2. Navigate to **Shops** page (bottom navigation)

**Look for in console:**
```
ShopsController: Initializing shop status WebSocket client...
Shop Status WebSocket Client: Attempting to connect to ws://localhost:8082
✓ Shop Status WebSocket Client: Connected to server
  Total listeners registered: X
Shop Status WebSocket: Client connected from /127.0.0.1:XXXXX
Shop Status WebSocket: Total clients: 1
✓ Listener added. Total listeners: X, Connected: true
ShopsController: Shop status listener registered
```

**Then you should see:**
```
ShopsController: Loading X approved shops
  - Khan Kitchen (ID: 2, Status: approved)
  - Sushi World (ID: 3, Status: approved)
  - ...
```

✅ **If you see this** → Client connected successfully  
❌ **If connection refused** → Server not running or wrong port

---

### **STEP 4: Open Admin Panel (Separate Window)**

1. Open a **NEW browser/window**
2. Login as **admin**
3. Go to **Manage Shops** section

**You should now have:**
- **Window 1:** User Panel (Shops page) - Keep this visible
- **Window 2:** Admin Panel - Keep this visible side-by-side

---

### **STEP 5: Test Shop Approval (Pending → Approved)**

In **Admin Panel:**
1. Find a shop with status **"pending"**
2. Change dropdown to **"approved"**
3. Click **"Details"** button

**Expected Console Output:**
```
╔════════════════════════════════════════════════════════╗
║ BROADCASTING SHOP STATUS CHANGE                        ║
╠════════════════════════════════════════════════════════╣
  Shop ID:     4
  Shop Name:   Pizza Palace
  New Status:  approved
  Owner ID:    2
  JSON:        {"shopId":4,"ownerId":2,"shopName":"Pizza Palace","status":"approved","action":"status_changed"}
  Connected clients: 1
  ✓ Sent to client: /127.0.0.1:XXXXX
  Successfully sent to 1/1 clients
╚════════════════════════════════════════════════════════╝
```

**Then:**
```
━━━ WebSocket Client: Message Received ━━━
Message: ShopStatusMessage{shopId=4, shopName='Pizza Palace', status='approved', action='status_changed'}
Notifying 3 listener(s)...
✓ Notified 3 listener(s) successfully
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Then:**
```
━━━ ShopsController: RECEIVED shop status change ━━━
Shop ID: 4
Shop Name: Pizza Palace
New Status: approved
Action: status_changed
Clearing and reloading shops...
ShopsController: Loading 3 approved shops
  - Khan Kitchen (ID: 2, Status: approved)
  - Sushi World (ID: 3, Status: approved)
  - Pizza Palace (ID: 4, Status: approved)
Shops reload complete!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Expected UI Behavior:**
- ✅ **Pizza Palace should INSTANTLY appear** in the Shops list (Window 1)
- ✅ **No page refresh needed**

---

### **STEP 6: Test Shop Rejection (Approved → Rejected)**

In **Admin Panel:**
1. Find an **approved** shop (e.g., "Sushi World")
2. Change dropdown to **"rejected"**
3. Click **"Details"** button

**Expected Console Output:**
```
╔════════════════════════════════════════════════════════╗
║ BROADCASTING SHOP STATUS CHANGE                        ║
╠════════════════════════════════════════════════════════╣
  Shop ID:     3
  Shop Name:   Sushi World
  New Status:  rejected
  Owner ID:    3
  JSON:        {"shopId":3,"ownerId":3,"shopName":"Sushi World","status":"rejected","action":"status_changed"}
  Connected clients: 1
  ✓ Sent to client: /127.0.0.1:XXXXX
  Successfully sent to 1/1 clients
╚════════════════════════════════════════════════════════╝
```

**Expected UI Behavior:**
- ✅ **Sushi World should INSTANTLY disappear** from Shops list (Window 1)
- ✅ **No page refresh needed**

---

### **STEP 7: Test with Menu/Food Items Pages**

Navigate to **Menu** or **Food Items** page in user panel.

**You should see:**
```
MenuController: Initializing shop status WebSocket client...
✓ Listener added. Total listeners: X, Connected: true
MenuController: Shop status listener registered
```

**Now when you change shop status in admin:**
- ✅ Food items from that shop should appear/disappear **INSTANTLY**

---

## 🐛 Troubleshooting

### Problem: "Shop status broadcast sent to 0 client(s)"

**Cause:** No clients connected to WebSocket server

**Solutions:**
1. Check user panel is on Shops/Menu/FoodItems page
2. Look for "Client connected" message in console
3. Restart application if server didn't start properly

---

### Problem: Client connection refused

**Console shows:**
```
Shop Status WebSocket Client Error: Connection refused: connect
```

**Cause:** Server not running

**Solutions:**
1. Check for "Shop Status WebSocket Server started successfully" message
2. Ensure port 8082 is not in use by another application
3. Run: `netstat -ano | findstr :8082` to check port availability

---

### Problem: Broadcast sent but UI doesn't update

**Console shows broadcast sent, but UI doesn't change**

**Debug Steps:**
1. Check console for "WebSocket Client: Message Received"
2. Check for "ShopsController: RECEIVED shop status change"
3. Check for "Clearing and reloading shops..."
4. Check for "ShopsController: Loading X approved shops"

**If any step is missing:**
- Listener not registered properly
- Connection lost
- UI thread issue (check for Platform.runLater errors)

---

### Problem: Listener count is 0

**Console shows:**
```
✓ Listener added. Total listeners: 0, Connected: false
```

**Cause:** Listener added before connection established

**Solution:** Connection delay of 1000ms should handle this, but if not:
- Increase delay in controller initialize() from 1000ms to 2000ms

---

## ✅ Success Criteria

### Required Console Messages (in order):

1. **Server Startup:**
   ```
   ✓ Shop Status WebSocket Server started on port 8082
   ```

2. **Client Connection:**
   ```
   ✓ Shop Status WebSocket Client: Connected to server
   Shop Status WebSocket: Total clients: 1
   ```

3. **Listener Registration:**
   ```
   ✓ Listener added. Total listeners: 3, Connected: true
   ```

4. **Broadcast Sent:**
   ```
   Successfully sent to 1/1 clients
   ```

5. **Client Receives:**
   ```
   ✓ Notified 3 listener(s) successfully
   ```

6. **UI Updates:**
   ```
   Clearing and reloading shops...
   Shops reload complete!
   ```

### Required UI Behavior:

- ✅ Shop appears **INSTANTLY** when approved
- ✅ Shop disappears **INSTANTLY** when rejected/pending
- ✅ Food items appear **INSTANTLY** when shop approved
- ✅ Food items disappear **INSTANTLY** when shop rejected
- ✅ Works across all pages: Shops, Menu, Food Items
- ✅ No manual refresh needed

---

## 📊 Example Complete Flow

```
[USER OPENS SHOPS PAGE]
→ ShopsController: Loading 2 approved shops
  - Khan Kitchen (ID: 2, Status: approved)
  - Sushi World (ID: 3, Status: approved)

[ADMIN APPROVES "PIZZA PALACE"]
→ Broadcasting shop status change: Pizza Palace → approved
→ Successfully sent to 1/1 clients
→ WebSocket Client: Message Received
→ ShopsController: RECEIVED shop status change
→ Clearing and reloading shops...
→ ShopsController: Loading 3 approved shops
  - Khan Kitchen (ID: 2, Status: approved)
  - Sushi World (ID: 3, Status: approved)
  - Pizza Palace (ID: 4, Status: approved)  ← NEW!
→ Shops reload complete!

[USER SEES]
→ Pizza Palace card appears INSTANTLY (no refresh)
```

---

## 🎯 What to Share if Not Working

If still not working, please share:

1. **Full console output** from application start
2. **Screenshot** of both windows (admin + user)
3. **Specific step where it fails** (which of STEP 1-7)
4. **Error messages** if any

---

## 📝 Summary

The system is now fully instrumented with detailed logging. Every step of the WebSocket communication is logged:
- Server broadcasts
- Client receives
- Listeners notified
- UI updates

Follow the testing steps above and the console will tell you exactly what's happening at each stage!
