@echo off
setlocal
cd /d "%~dp0"
chcp 65001 >nul
title Web Browser Client (Lab 4 LTM)

echo ========================================================
echo   🌐 KHỞI ĐỘNG TRÌNH DUYỆT WEB BROWSER
echo ========================================================
echo.

echo Dang bien dich va khoi chay Trinh duyet...
call .\mvnw.cmd javafx:run -Pclient

if %ERRORLEVEL% neq 0 (
    echo.
    echo [LOI] Khong the khoi chay Trinh duyet! Vui long kiem tra moi truong JDK 21.
    pause
)
