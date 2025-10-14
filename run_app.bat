@echo off
title UniEats - Food Delivery App
color 0A
echo.
echo ========================================
echo    UniEats - Food Delivery Application
echo ========================================
echo.

echo Starting UniEats application...
echo.

REM Use Maven wrapper instead of requiring Maven installation
echo [1/3] Compiling application...
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    echo Please check the error messages above
    pause
    exit /b 1
)

echo [2/3] Application compiled successfully
echo [3/3] Starting UniEats...
echo.

REM Run the application with crash prevention
call mvnw.cmd javafx:run -q

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Application failed to start
    echo Trying alternative method...
    echo.
    
    REM Try with exec:java
    call mvnw.cmd exec:java -Dexec.mainClass="com.unieats.UniEatsLauncher" -q
    
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: All methods failed
        echo Please check the error messages above
        pause
        exit /b 1
    )
)

echo.
echo Application closed.
pause
