@echo off
echo ========================================
echo   UniEats Admin Dashboard Error Fix
echo ========================================

echo [1/5] Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java 17 or higher.
    pause
    exit /b 1
)
echo ✅ Java found!

echo [2/5] Cleaning previous builds...
if exist target rmdir /s /q target
echo ✅ Cleaned build directories

echo [3/5] Compiling with Maven...
mvn clean compile -q
if %errorlevel% neq 0 (
    echo ❌ Compilation failed!
    echo Running verbose compilation to see errors...
    mvn compile
    pause
    exit /b 1
)
echo ✅ Compilation successful!

echo [4/5] Checking database...
if not exist unieats.db (
    echo ⚠️  Database not found, will be created on first run
) else (
    echo ✅ Database exists
)

echo [5/5] Starting application...
echo ✅ All checks passed! Starting UniEats...
mvn javafx:run

pause
