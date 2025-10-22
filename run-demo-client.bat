@echo off
REM Real-Time Stock Demo - Client Launcher

echo =================================================================
echo   Real-Time Stock Update Demo - Starting Client
echo =================================================================
echo.
echo Make sure the server is running first!
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

echo [2/2] Starting JavaFX client...
echo.
call mvn javafx:run -Djavafx.mainClass="com.unieats.demo.RealTimeStockDemoClient"

pause
