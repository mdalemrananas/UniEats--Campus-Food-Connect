@echo off
title UniEats - Complete Solution
color 0C
echo.
echo ========================================
echo    UniEats - Complete Solution
echo ========================================
echo.

REM Set console to handle Unicode properly
chcp 65001 >nul

echo This script will fix all issues and run your UniEats app with seller features.
echo.

REM Check Java
echo [1/7] Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java 17+ from https://adoptium.net/
    pause
    exit /b 1
)
echo ✅ Java found!

REM Find and set JAVA_HOME
echo [2/7] Setting up JAVA_HOME...
set JAVA_HOME_FOUND=false

REM Try common locations
for %%d in (
    "C:\Program Files\Java\jdk-19"
    "C:\Program Files\Java\jdk-17" 
    "C:\Program Files\Java\jdk-21"
    "C:\Program Files\Eclipse Adoptium\jdk-19"
    "C:\Program Files\Eclipse Adoptium\jdk-17"
    "C:\Program Files\Eclipse Adoptium\jdk-21"
) do (
    if exist %%d (
        set JAVA_HOME=%%d
        set JAVA_HOME_FOUND=true
        echo ✅ Found Java at: %%d
        goto java_home_set
    )
)

if "%JAVA_HOME_FOUND%"=="false" (
    echo ⚠️  Could not find JAVA_HOME automatically
    echo Setting JAVA_HOME to current Java installation...
    for /f "tokens=*" %%i in ('where java') do (
        set JAVA_PATH=%%i
        goto set_java_home_from_path
    )
)

:set_java_home_from_path
if defined JAVA_PATH (
    for %%i in ("%JAVA_PATH%") do set JAVA_HOME=%%~dpi..
    echo ✅ Set JAVA_HOME to: %JAVA_HOME%
) else (
    echo ❌ Could not set JAVA_HOME
    pause
    exit /b 1
)

:java_home_set
set PATH=%JAVA_HOME%\bin;%PATH%

REM Clean build
echo [3/7] Cleaning previous builds...
if exist "target" rmdir /s /q "target" >nul 2>&1
if exist "build" rmdir /s /q "build" >nul 2>&1
if exist "simple_build" rmdir /s /q "simple_build" >nul 2>&1
echo ✅ Cleaned build directories

REM Try Maven compilation
echo [4/7] Attempting Maven compilation...
call mvnw.cmd clean compile -q >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Maven compilation successful
    set USE_MAVEN=true
    goto compilation_done
) else (
    echo ⚠️  Maven compilation failed, trying direct Java compilation...
    set USE_MAVEN=false
)

REM Direct Java compilation
echo [5/7] Direct Java compilation...
if not exist "build\classes" mkdir "build\classes"

REM Copy resources
xcopy "src\main\resources\*" "build\classes\" /E /I /Y >nul 2>&1

REM Compile all Java files
for /r "src\main\java" %%f in (*.java) do (
    javac -d "build\classes" -cp "build\classes" "%%f" >nul 2>&1
    if %errorlevel% neq 0 (
        echo ❌ Failed to compile %%f
        pause
        exit /b 1
    )
)
echo ✅ Direct Java compilation successful

:compilation_done

REM Set crash prevention
echo [6/7] Setting up crash prevention...
set MAVEN_OPTS=-Djavafx.verbose=false -Dprism.order=sw -Djava.awt.headless=false -Dsun.java2d.opengl=false -Dsun.java2d.d3d=false -XX:+UseG1GC -Xmx2g

REM Launch application
echo [7/7] Launching UniEats application...
echo.
echo ========================================
echo    🚀 UniEats is starting...
echo ========================================
echo.
echo Your seller features will be available:
echo 🍔 Food Post - Add new food items
echo 📦 Inventory Management - Edit/delete items  
echo 📋 Order Management - Handle customer orders
echo.

if "%USE_MAVEN%"=="true" (
    call mvnw.cmd javafx:run -q
) else (
    java -cp "build\classes" ^
         -Djavafx.verbose=false ^
         -Dprism.order=sw ^
         -Djava.awt.headless=false ^
         -Dsun.java2d.opengl=false ^
         -Dsun.java2d.d3d=false ^
         -XX:+UseG1GC ^
         -Xmx2g ^
         com.unieats.UniEatsApp
)

if %errorlevel% neq 0 (
    echo.
    echo ❌ Application failed to start
    echo Trying alternative launcher...
    java -cp "build\classes" ^
         -Djavafx.verbose=false ^
         -Dprism.order=sw ^
         -Djava.awt.headless=false ^
         -Dsun.java2d.opengl=false ^
         -Dsun.java2d.d3d=false ^
         -XX:+UseG1GC ^
         -Xmx2g ^
         com.unieats.UniEatsLauncher
)

echo.
echo ========================================
echo    Application finished
echo ========================================
pause