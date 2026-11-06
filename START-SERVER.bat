@echo off
setlocal
cd /d "%~dp0"
chcp 65001 >nul
title HTTP Application Server Dashboard (Lab 4 LTM)

echo ========================================================
echo   ⚡ KHỞI ĐỘNG HTTP WEB SERVER (CỔNG 8080)
echo ========================================================
echo.

:: Kiểm tra và giải phóng cổng 8080 nếu bị chiếm dụng
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo [CANH BAO] Cong 8080 dang bi chiem boi PID %%a, dang giai phong...
    taskkill /f /pid %%a > nul 2>&1
)

echo Dang bien dich va khoi chay Server Dashboard...
call .\mvnw.cmd javafx:run -Pserver

if %ERRORLEVEL% neq 0 (
    echo.
    echo [LOI] Khong the khoi chay Server! Vui long kiem tra moi truong JDK 21.
    pause
)
