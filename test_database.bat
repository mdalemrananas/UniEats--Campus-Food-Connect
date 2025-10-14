@echo off
title UniEats Database Test
color 0B
echo.
echo ========================================
echo    UniEats Database Test
echo ========================================
echo.

echo Testing database functionality...
echo.

REM Compile and run database test
call mvn compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo Running database test...
call mvn exec:java -Dexec.mainClass="com.unieats.util.DatabaseTest" -q

echo.
echo Database test completed.
pause
