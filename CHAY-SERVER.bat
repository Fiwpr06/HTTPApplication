@echo off
chcp 65001 >nul
title [LAB 4] HTTP MULTI-SERVICE WEB SERVER
cd /d "%~dp0"
cls
echo ======================================================================
echo    MAY CHU HTTP MULTI-SERVICE WEB SERVER (LAB 4 - LTM VKU)
echo ======================================================================
echo    Phuc vu 3 dich vu:
echo      [1] Stream Nhac MP3     (.mp3 / Range 206)
echo      [2] Stream Video MP4    (.mp4 / Range 206)
echo      [3] Xem Tai Lieu PDF    (.pdf / HTTP 200)
echo.
echo    Dang khoi dong may chu tai cong 8080...
echo ======================================================================
call .\mvnw.cmd javafx:run
pause
