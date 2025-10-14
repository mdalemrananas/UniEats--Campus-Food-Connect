@echo off
title UniEats - Food Delivery App
color 0A
echo.
echo ========================================
echo    UniEats - Food Delivery Application
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

echo Java found. Checking Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven
    pause
    exit /b 1
)

echo Maven found. Starting application...
echo.

REM Clean and compile
echo [1/4] Cleaning previous build...
call mvn clean -q

echo [2/4] Compiling application...
call mvn compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo [3/4] Starting UniEats with crash prevention...
echo.

REM Set environment variables for crash prevention
set MAVEN_OPTS=-Djavafx.verbose=false
set MAVEN_OPTS=%MAVEN_OPTS% -Dprism.verbose=false
set MAVEN_OPTS=%MAVEN_OPTS% -Dprism.order=sw
set MAVEN_OPTS=%MAVEN_OPTS% -Djava.awt.headless=false
set MAVEN_OPTS=%MAVEN_OPTS% -Dsun.java2d.opengl=false
set MAVEN_OPTS=%MAVEN_OPTS% -Dsun.java2d.d3d=false
set MAVEN_OPTS=%MAVEN_OPTS% -XX:+UseG1GC
set MAVEN_OPTS=%MAVEN_OPTS% -Xmx2g
set MAVEN_OPTS=%MAVEN_OPTS% -XX:+UnlockExperimentalVMOptions
set MAVEN_OPTS=%MAVEN_OPTS% -XX:+UseZGC

echo [4/4] Launching application...
echo.
echo Application is starting... Please wait.
echo If you see any error messages, please report them.
echo.

REM Run the application using the new launcher
call mvn exec:java -Dexec.mainClass="com.unieats.UniEatsLauncher" -q

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Application failed to start
    echo Trying alternative launch method...
    echo.
    
    REM Try with javafx:run
    call mvn javafx:run -q
    
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: All launch methods failed
        echo Please check the error messages above
        pause
        exit /b 1
    )
)

echo.
echo Application closed.
pause
