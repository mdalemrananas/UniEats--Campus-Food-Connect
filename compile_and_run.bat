@echo off
title UniEats - Direct Java Compilation
color 0E
echo.
echo ========================================
echo    UniEats - Direct Java Compilation
echo ========================================
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

echo Java found! Starting compilation...
echo.

REM Create output directory
if not exist "build" mkdir build
if not exist "build\classes" mkdir build\classes

REM Set classpath for JavaFX (you'll need to download these JARs)
set CLASSPATH=build\classes

REM Compile Java files
echo [1/3] Compiling Java source files...
javac -d build\classes -cp "build\classes" src\main\java\com\unieats\*.java src\main\java\com\unieats\controllers\*.java src\main\java\com\unieats\dao\*.java src\main\java\com\unieats\util\*.java

if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    echo This method requires JavaFX JARs to be downloaded
    echo Please use the Maven method instead
    pause
    exit /b 1
)

echo [2/3] Compilation successful
echo [3/3] Running application...
echo.

REM Run the application
java -cp "build\classes" com.unieats.UniEatsLauncher

echo.
echo Application finished.
pause
