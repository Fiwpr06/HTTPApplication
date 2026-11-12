@echo off
chcp 65001 >nul
title MEDIAHUB - LOCAL MEDIA AND DOCUMENT SERVER
cd /d "%~dp0"
cls
echo ======================================================================
echo    MEDIAHUB - LOCAL MEDIA AND DOCUMENT SERVER
echo ======================================================================
echo    Cac dich vu tich hop:
echo      [1] Stream Nhac MP3/WAV (.mp3, .wav / Range 206)
echo      [2] Stream Video MP4    (.mp4 / Range 206)
echo      [3] Xem Tai Lieu PDF    (.pdf / HTTP 200)
echo.
echo    Dang khoi dong may chu tai cong 8080...
echo ======================================================================
call .\mvnw.cmd javafx:run
pause
