@echo off
title UniEats - Final Working Solution
color 0A
echo.
echo ========================================
echo    🚀 UniEats - Final Working Solution
echo ========================================
echo.

REM Set JAVA_HOME to the found Java installation
set JAVA_HOME=C:\Program Files\Java\jdk-19
set PATH=%JAVA_HOME%\bin;%PATH%

echo ✅ Java configured: %JAVA_HOME%
echo.

REM Clean and create build directory
echo [1/4] Setting up build environment...
if exist "final_build" rmdir /s /q "final_build" >nul 2>&1
mkdir "final_build\classes" >nul 2>&1
echo ✅ Build directory ready

REM Copy all resources
echo [2/4] Copying resources...
xcopy "src\main\resources\*" "final_build\classes\" /E /I /Y >nul 2>&1
echo ✅ Resources copied

REM Compile all Java files
echo [3/4] Compiling Java source files...
for /r "src\main\java" %%f in (*.java) do (
    javac -d "final_build\classes" -cp "final_build\classes" "%%f" >nul 2>&1
    if %errorlevel% neq 0 (
        echo ❌ Compilation error in %%f
        echo Trying to compile with verbose output...
        javac -d "final_build\classes" -cp "final_build\classes" "%%f"
        pause
        exit /b 1
    )
)
echo ✅ All Java files compiled successfully

REM Launch the application
echo [4/4] 🚀 Launching UniEats...
echo.
echo ========================================
echo    🎉 UniEats is starting!
echo ========================================
echo.
echo Your seller features are ready:
echo 🍔 Food Post - Add new food items
echo 📦 Inventory Management - Edit/delete items
echo 📋 Order Management - Handle customer orders
echo.
echo Navigate to the seller dashboard to test them!
echo.

java -cp "final_build\classes" ^
     -Djavafx.verbose=false ^
     -Dprism.order=sw ^
     -Djava.awt.headless=false ^
     -Dsun.java2d.opengl=false ^
     -Dsun.java2d.d3d=false ^
     -XX:+UseG1GC ^
     -Xmx2g ^
     com.unieats.UniEatsApp

echo.
echo ========================================
echo    Application finished
echo ========================================
pause
