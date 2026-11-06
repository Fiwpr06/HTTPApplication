@echo off
chcp 65001 > nul
title WebServerPro — HTTP Web Server Dashboard (Lab 4 LTM)
cd /d "%~dp0"

echo ========================================================
echo   ⚡ KHỞI ĐỘNG HTTP WEB SERVER DASHBOARD (CỔNG 8080)
echo ========================================================
echo.

:: Kiểm tra và giải phóng cổng 8080 nếu bị chiếm dụng
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo [CANH BAO] Cong 8080 dang bi chiem boi PID %%a, dang giai phong...
    taskkill /f /pid %%a > nul 2>&1
)

echo Dang bien dich va khoi chay Server Dashboard...
call mvnw.cmd javafx:run -Djavafx.mainClass="com.fiwpr06.websocket/com.fiwpr06.websocket.server.ServerLauncher"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the khoi chay Server! Vui long kiem tra lai moi truong Java.
    pause
)
