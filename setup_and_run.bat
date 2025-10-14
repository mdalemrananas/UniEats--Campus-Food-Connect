@echo off
title UniEats Setup and Run
color 0C
echo.
echo ========================================
echo    UniEats - Setup and Run
echo ========================================
echo.

REM Check if Java is installed
echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo.
    echo Please install Java 17 or higher from:
    echo https://adoptium.net/
    echo.
    echo After installation, restart this script
    pause
    exit /b 1
)

echo Java found! Checking version...
java -version

REM Try to find Java installation
echo.
echo Looking for Java installation...
for /f "tokens=2*" %%i in ('reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\JDK" /s /v JavaHome 2^>nul ^| find "JavaHome"') do (
    set JAVA_HOME=%%j
    goto found
)

for /f "tokens=2*" %%i in ('reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\JRE" /s /v JavaHome 2^>nul ^| find "JavaHome"') do (
    set JAVA_HOME=%%j
    goto found
)

echo WARNING: Could not find JAVA_HOME automatically
echo Trying common locations...

if exist "C:\Program Files\Java\jdk-17" (
    set JAVA_HOME=C:\Program Files\Java\jdk-17
    goto found
)

if exist "C:\Program Files\Java\jdk-21" (
    set JAVA_HOME=C:\Program Files\Java\jdk-21
    goto found
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-17" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
    goto found
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-21" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21
    goto found
)

echo ERROR: Could not find Java installation
echo Please set JAVA_HOME manually or install Java properly
pause
exit /b 1

:found
echo Found Java at: %JAVA_HOME%
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo ========================================
echo    Starting UniEats Application
echo ========================================
echo.

REM Compile the application
echo [1/4] Compiling application...
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    echo Please check the error messages above
    pause
    exit /b 1
)

echo [2/4] Compilation successful
echo [3/4] Starting UniEats with crash prevention...
echo.

REM Set crash prevention environment variables
set MAVEN_OPTS=-Djavafx.verbose=false -Dprism.order=sw -Djava.awt.headless=false -Dsun.java2d.opengl=false -Dsun.java2d.d3d=false -XX:+UseG1GC -Xmx2g

echo [4/4] Launching application...
echo.

REM Try to run the application
call mvnw.cmd javafx:run -q

if %errorlevel% neq 0 (
    echo.
    echo Trying alternative launch method...
    call mvnw.cmd exec:java -Dexec.mainClass="com.unieats.UniEatsLauncher" -q
    
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: Application failed to start
        echo.
        echo Possible solutions:
        echo 1. Make sure Java 17+ is installed
        echo 2. Check if all files are present
        echo 3. Try running from your IDE instead
        echo.
        pause
        exit /b 1
    )
)

echo.
echo Application closed successfully.
pause
