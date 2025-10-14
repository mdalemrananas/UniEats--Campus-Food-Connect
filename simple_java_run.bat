@echo off
title UniEats - Simple Java Run
color 0B
echo.
echo ========================================
echo    UniEats - Simple Java Execution
echo ========================================
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from https://adoptium.net/
    pause
    exit /b 1
)

echo Java found! Creating simple build...
echo.

REM Create build directory
if not exist "simple_build" mkdir "simple_build"
if not exist "simple_build\classes" mkdir "simple_build\classes"

REM Copy resources
echo Copying resources...
xcopy "src\main\resources\*" "simple_build\classes\" /E /I /Y >nul 2>&1

REM Compile main classes
echo Compiling main classes...
javac -d "simple_build\classes" -cp "simple_build\classes" src\main\java\com\unieats\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile main classes
    pause
    exit /b 1
)

REM Compile controllers
echo Compiling controllers...
javac -d "simple_build\classes" -cp "simple_build\classes" src\main\java\com\unieats\controllers\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile controllers
    pause
    exit /b 1
)

REM Compile DAO classes
echo Compiling DAO classes...
javac -d "simple_build\classes" -cp "simple_build\classes" src\main\java\com\unieats\dao\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile DAO classes
    pause
    exit /b 1
)

REM Compile utility classes
echo Compiling utility classes...
javac -d "simple_build\classes" -cp "simple_build\classes" src\main\java\com\unieats\util\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile utility classes
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Set crash prevention properties
echo Starting UniEats with crash prevention...
echo.

REM Run the application
java -cp "simple_build\classes" ^
     -Djavafx.verbose=false ^
     -Dprism.order=sw ^
     -Djava.awt.headless=false ^
     -Dsun.java2d.opengl=false ^
     -Dsun.java2d.d3d=false ^
     -XX:+UseG1GC ^
     -Xmx2g ^
     com.unieats.UniEatsLauncher

if %errorlevel% neq 0 (
    echo.
    echo Trying with original launcher...
    java -cp "simple_build\classes" ^
         -Djavafx.verbose=false ^
         -Dprism.order=sw ^
         -Djava.awt.headless=false ^
         -Dsun.java2d.opengl=false ^
         -Dsun.java2d.d3d=false ^
         -XX:+UseG1GC ^
         -Xmx2g ^
         com.unieats.UniEatsApp
)

echo.
echo Application finished.
pause
