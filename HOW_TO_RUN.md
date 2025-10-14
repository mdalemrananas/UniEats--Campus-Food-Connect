# 🚀 UniEats - How to Run Perfectly

## 📋 Prerequisites
- **Java 17 or higher** (required)
- **Maven 3.6+** (required)
- **Windows 10/11** (tested on)

## 🎯 Quick Start (Recommended)

### Method 1: Use the Batch File (Easiest)
1. **Double-click** `start_unieats.bat`
2. **Wait** for the application to compile and start
3. **Enjoy** your UniEats app!

### Method 2: Command Line
```bash
# Navigate to project directory
cd "D:\UIU\11th trimester\AOOP\Project_file\Original\UniEats"

# Run the application
mvn clean compile javafx:run
```

## 🔧 If You Get Crashes

### Solution 1: Use the New Launcher
The app now uses `UniEatsLauncher.java` which has better crash prevention.

### Solution 2: Test Database First
1. **Double-click** `test_database.bat`
2. **Check** if database operations work
3. **If database works**, the main app should work too

### Solution 3: Manual JVM Arguments
If running from IDE, add these VM options:
```
-Djavafx.verbose=false
-Dprism.order=sw
-Djava.awt.headless=false
-Dsun.java2d.opengl=false
-Dsun.java2d.d3d=false
-XX:+UseG1GC
-Xmx2g
```

## 🍔 Testing Seller Features

Once the app is running:

1. **Navigate to Seller Dashboard**
   - Sign in as a seller
   - Go to the stall/dashboard page

2. **Test Food Post**
   - Click "🍔 Food Post" button
   - Add a food item (name + price)
   - Should show "Food item added successfully!"

3. **Test Inventory Management**
   - Click "📦 Inventory" button
   - Should show your food items
   - Try editing prices (double-click)
   - Try deleting items

4. **Test Order Management**
   - Click "📋 Order Management" button
   - Click "Add Sample Orders" for test data
   - Try changing order statuses

## 🗄️ Database Information

The app uses SQLite database (`unieats.db`) with these tables:
- `food_items` - Your food menu items
- `seller_orders` - Orders for your shop
- `shops` - Shop information
- `users` - User accounts

## 🐛 Troubleshooting

### Problem: App Won't Start
**Solution**: Use `start_unieats.bat` - it has comprehensive error handling

### Problem: Database Errors
**Solution**: Run `test_database.bat` to check database functionality

### Problem: JavaFX Crashes
**Solution**: The new launcher has crash prevention built-in

### Problem: Buttons Don't Work
**Solution**: Make sure you're signed in as a seller user

## 📁 File Structure

```
UniEats/
├── start_unieats.bat          # Main launcher (USE THIS)
├── test_database.bat          # Database test
├── src/main/java/com/unieats/
│   ├── UniEatsLauncher.java   # New robust launcher
│   ├── controllers/           # All controllers
│   └── util/                  # Database utilities
└── src/main/resources/fxml/   # UI files
```

## ✅ What Should Work

After following these steps, you should have:

1. ✅ **Stable app** that doesn't crash
2. ✅ **Food Post** - Add new food items
3. ✅ **Inventory Management** - Edit/delete food items
4. ✅ **Order Management** - Handle customer orders
5. ✅ **Database** - All data persists correctly

## 🎉 Success Indicators

You'll know it's working when:
- App starts without crashes
- You can add food items successfully
- Inventory shows your items
- Order management displays orders
- All buttons respond properly

## 📞 Need Help?

If you're still having issues:
1. Check the console output for error messages
2. Try the database test first
3. Make sure Java and Maven are properly installed
4. Use the batch file launcher

---

**Remember**: Always use `start_unieats.bat` for the best experience! 🚀
