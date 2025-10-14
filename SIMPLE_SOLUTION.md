# 🚀 Simple Solution - Get UniEats Running

## 🎯 The Problem
Your app isn't running because of Java/Maven configuration issues.

## ✅ Simple Solution (3 Steps)

### Step 1: Install Java (if not installed)
1. Go to: https://adoptium.net/
2. Download **Java 17** or **Java 21**
3. Install it
4. **Restart your computer**

### Step 2: Run the Setup Script
1. **Double-click** `setup_and_run.bat`
2. **Wait** for it to complete
3. **Follow** any instructions it shows

### Step 3: If Still Not Working
Try these alternatives:

#### Option A: Use Your IDE
1. **Open** the project in IntelliJ IDEA or Eclipse
2. **Run** `UniEatsLauncher.java` directly
3. **Add VM options**:
   ```
   -Djavafx.verbose=false
   -Dprism.order=sw
   -Djava.awt.headless=false
   -Dsun.java2d.opengl=false
   -Dsun.java2d.d3d=false
   -XX:+UseG1GC
   -Xmx2g
   ```

#### Option B: Manual Maven
1. **Install Maven** from: https://maven.apache.org/
2. **Add Maven to PATH**
3. **Run**: `mvn clean compile javafx:run`

## 🔧 Quick Fix Commands

Open **Command Prompt** in your project folder and try:

```cmd
# Check Java
java -version

# Check if Maven wrapper works
mvnw.cmd --version

# Try to compile
mvnw.cmd clean compile

# Try to run
mvnw.cmd javafx:run
```

## 🎯 What Should Happen

When it works, you should see:
1. ✅ Compilation successful
2. ✅ JavaFX application window opens
3. ✅ UniEats interface appears
4. ✅ You can navigate to seller dashboard
5. ✅ All three buttons work (Food Post, Inventory, Orders)

## 🆘 Still Not Working?

**Tell me exactly what error message you see** and I'll help you fix it!

Common issues:
- "Java not found" → Install Java
- "Maven not found" → Use mvnw.cmd instead
- "JAVA_HOME not set" → Run setup_and_run.bat
- "Compilation failed" → Check Java version (need 17+)

## 🎉 Success!

Once it's running, test your seller features:
1. **Sign in** as seller
2. **Go to** seller dashboard
3. **Click** Food Post → Add item
4. **Click** Inventory → View items
5. **Click** Order Management → Add sample orders

**Your app will work perfectly!** 🚀
