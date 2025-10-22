# Troubleshooting Guide

## Common Issues and Solutions

### 1. Gson Reflection Error (FIXED)

**Error:**
```
Failed to broadcast shop status change: Failed making field 'com.unieats.models.ShopStatusMessage#shopId' accessible; either increase its visibility or write a custom TypeAdapter for its declaring type.
```

**Cause:** Java module system blocks Gson from accessing private fields via reflection.

**Solution:** Open the packages to Gson in `module-info.java`:
```java
opens com.unieats.models to com.google.gson;
opens com.unieats.websocket to com.google.gson;
```

**Status:** ✅ Fixed

---

### 2. Timestamp Parsing Errors (FIXED)

**Error:**
```
Error parsing timestamps: Text '2025-08-17T20:54:12.704634700' could not be parsed at index 10
```

**Cause:** Database stores ISO-8601 format with nanoseconds, but parser expected SQL datetime format.

**Solution:** Implemented flexible datetime parser that handles multiple formats.

**Status:** ✅ Fixed

---

### 3. WebSocket Connection Issues

**Error:**
```
Failed to connect to Shop Status WebSocket Server: Connection refused
```

**Possible Causes:**
1. WebSocket server not started
2. Port 8082 already in use
3. Firewall blocking connection

**Solutions:**
1. Ensure `UniEatsApp` starts the WebSocket server
2. Check if port is available: `netstat -ano | findstr :8082`
3. Change port in both server and client if needed

---

### 4. Shop Status Not Updating in Real-Time

**Symptoms:**
- Admin changes shop status but user panel doesn't update
- No console messages about shop status changes

**Checklist:**
1. ✅ Check WebSocket server is running (console shows "Shop Status WebSocket Server started")
2. ✅ Check client connection (console shows "Client connected")
3. ✅ Verify admin panel calls `DatabaseManager.updateSellerAndShopStatus()`
4. ✅ Check broadcast message in console

**Debug Commands:**
```java
// In DatabaseManager after broadcast
System.out.println("Broadcast sent for shop: " + shopName);

// In MenuController listener
System.out.println("Received shop status: " + statusMsg);
```

---

### 5. Maven Compilation Errors

**Error:**
```
package com.google.gson is not visible
```

**Solution:**
1. Add Gson dependency in `pom.xml`:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

2. Add to `module-info.java`:
```java
requires com.google.gson;
```

3. Run: `mvn clean compile`

---

### 6. Food Items Not Appearing After Shop Approval

**Possible Causes:**
1. WebSocket not triggering refresh
2. SQL query filtering shops incorrectly
3. Food items not linked to shop properly

**Solution:**
Verify SQL query includes shop status filter:
```sql
SELECT fi.* FROM food_items fi 
JOIN shops s ON fi.shop_id = s.id 
WHERE s.status = 'approved'
```

---

## Testing Commands

### Run Application
```powershell
mvn javafx:run
```

### Clean and Compile
```powershell
mvn clean compile
```

### Check Port Availability
```powershell
# Windows
netstat -ano | findstr :8082

# Linux/Mac
lsof -i :8082
```

### View Logs
Check console output for:
- `✓ Shop Status WebSocket Server started on port 8082`
- `Shop Status WebSocket: Client connected`
- `Broadcasting shop status change: ShopStatusMessage{...}`
- `MenuController: Shop status changed - ...`

---

## Expected Behavior

### When Admin Approves Shop:
1. ✅ Database updates shop status to 'approved'
2. ✅ WebSocket broadcasts message to all clients
3. ✅ User panels receive message
4. ✅ Food items refresh automatically
5. ✅ Shop's food items become visible

### When Admin Rejects Shop:
1. ✅ Database updates shop status to 'rejected'
2. ✅ WebSocket broadcasts message
3. ✅ User panels receive message
4. ✅ Food items refresh automatically
5. ✅ Shop's food items disappear

---

## Module System Configuration

### Required in `module-info.java`:
```java
module com.unieats {
    // Dependencies
    requires com.google.gson;
    requires org.java_websocket;
    
    // Open packages for reflection
    opens com.unieats.models to com.google.gson;
    opens com.unieats.websocket to com.google.gson;
}
```

---

## Contact & Support

For additional issues:
1. Check console output for error messages
2. Verify all WebSocket connections are established
3. Test with multiple user panels simultaneously
4. Review `SHOP_STATUS_REALTIME_IMPLEMENTATION.md` for architecture details

**All known issues have been resolved!** ✅
