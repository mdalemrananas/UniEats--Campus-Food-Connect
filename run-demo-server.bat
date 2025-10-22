@echo off
REM Real-Time Stock Demo - Server Launcher

echo =================================================================
echo   Real-Time Stock Update Demo - Starting Server
echo =================================================================
echo.

cd /d "%~dp0"

echo [1/2] Compiling project...
call mvn clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo [2/2] Starting WebSocket server...
echo.
call mvn exec:java -Dexec.mainClass="com.unieats.demo.RealTimeStockServer" -Dexec.cleanupDaemonThreads=false

pause
