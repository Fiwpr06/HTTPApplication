@echo off
chcp 65001 > nul
title WebBrowserPro — Chrome/CocCoc Style Browser (Lab 4 LTM)
cd /d "%~dp0"

echo ========================================================
echo   🌐 KHỞI ĐỘNG TRÌNH DUYỆT WEB BROWSER (CHROME STYLE)
echo ========================================================
echo.

echo Dang bien dich va khoi chay Trinh duyet Web...
call mvnw.cmd javafx:run -Djavafx.mainClass="com.fiwpr06.httpapplication/com.fiwpr06.httpapplication.client.BrowserLauncher"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the khoi chay Trinh duyet! Vui long kiem tra lai moi truong Java.
    pause
)
