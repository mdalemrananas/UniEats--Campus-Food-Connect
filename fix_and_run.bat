@echo off
title UniEats - Fix and Run
color 0A
echo.
echo ========================================
echo    UniEats - Fix All Issues and Run
echo ========================================
echo.

REM Find Java installation
echo [1/6] Finding Java installation...
set JAVA_FOUND=false

REM Check common Java locations
if exist "C:\Program Files\Java\jdk-19" (
    set JAVA_HOME=C:\Program Files\Java\jdk-19
    set JAVA_FOUND=true
    goto java_found
)

if exist "C:\Program Files\Java\jdk-17" (
    set JAVA_HOME=C:\Program Files\Java\jdk-17
    set JAVA_FOUND=true
    goto java_found
)

if exist "C:\Program Files\Java\jdk-21" (
    set JAVA_HOME=C:\Program Files\Java\jdk-21
    set JAVA_FOUND=true
    goto java_found
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-19" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-19
    set JAVA_FOUND=true
    goto java_found
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-17" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
    set JAVA_FOUND=true
    goto java_found
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-21" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21
    set JAVA_FOUND=true
    goto java_found
)

REM Try to find from registry
for /f "tokens=2*" %%i in ('reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\JDK" /s /v JavaHome 2^>nul ^| find "JavaHome"') do (
    set JAVA_HOME=%%j
    set JAVA_FOUND=true
    goto java_found
)

echo ERROR: Could not find Java installation
echo Please install Java 17 or higher from https://adoptium.net/
pause
exit /b 1

:java_found
echo Found Java at: %JAVA_HOME%
set PATH=%JAVA_HOME%\bin;%PATH%

REM Verify Java works
echo [2/6] Verifying Java installation...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java verification failed
    pause
    exit /b 1
)

REM Clean previous builds
echo [3/6] Cleaning previous builds...
if exist "target" rmdir /s /q "target"
if exist "build" rmdir /s /q "build"

REM Compile the project
echo [4/6] Compiling project...
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    echo Trying alternative compilation method...
    
    REM Try direct javac compilation
    if not exist "build\classes" mkdir "build\classes"
    
    REM Find all Java files and compile them
    for /r "src\main\java" %%f in (*.java) do (
        echo Compiling %%f
        javac -d "build\classes" -cp "build\classes" "%%f"
        if %errorlevel% neq 0 (
            echo ERROR: Failed to compile %%f
            pause
            exit /b 1
        )
    )
    
    echo Compilation completed using direct javac
) else (
    echo Compilation successful using Maven
)

REM Copy resources
echo [5/6] Copying resources...
if not exist "build\classes" mkdir "build\classes"
xcopy "src\main\resources\*" "build\classes\" /E /I /Y >nul 2>&1

REM Set crash prevention environment variables
echo [6/6] Setting up crash prevention...
set MAVEN_OPTS=-Djavafx.verbose=false -Dprism.order=sw -Djava.awt.headless=false -Dsun.java2d.opengl=false -Dsun.java2d.d3d=false -XX:+UseG1GC -Xmx2g

echo.
echo ========================================
echo    Starting UniEats Application
echo ========================================
echo.

REM Try to run with Maven first
echo Attempting to start with Maven...
call mvnw.cmd javafx:run -q

if %errorlevel% neq 0 (
    echo Maven launch failed, trying direct Java execution...
    
    REM Try direct Java execution
    java -cp "build\classes;target\classes" -Djavafx.verbose=false -Dprism.order=sw -Djava.awt.headless=false -Dsun.java2d.opengl=false -Dsun.java2d.d3d=false -XX:+UseG1GC -Xmx2g com.unieats.UniEatsLauncher
    
    if %errorlevel% neq 0 (
        echo Direct Java execution failed, trying original launcher...
        java -cp "build\classes;target\classes" -Djavafx.verbose=false -Dprism.order=sw -Djava.awt.headless=false -Dsun.java2d.opengl=false -Dsun.java2d.d3d=false -XX:+UseG1GC -Xmx2g com.unieats.UniEatsApp
    )
)

echo.
echo Application finished.
pause
