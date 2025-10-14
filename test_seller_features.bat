@echo off
title Test Seller Features
color 0E
echo.
echo ========================================
echo    Testing Seller Features
echo ========================================
echo.

REM Set JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-19
set PATH=%JAVA_HOME%\bin;%PATH%

REM Create test build
if not exist "test_build\classes" mkdir "test_build\classes"
xcopy "src\main\resources\*" "test_build\classes\" /E /I /Y >nul 2>&1

REM Compile test classes
echo Compiling test classes...
javac -d "test_build\classes" -cp "test_build\classes" src\main\java\com\unieats\util\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile utility classes
    pause
    exit /b 1
)

echo Testing database functionality...
java -cp "test_build\classes" com.unieats.util.DatabaseTest

echo.
echo Database test completed!
pause
