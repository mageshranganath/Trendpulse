@echo off
setlocal
cd /d "%~dp0"

echo Building signed release APK...
call gradlew.bat :app:assembleRelease
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)

echo.
echo Done. Output:
echo app\build\outputs\apk\release\app-release.apk
exit /b 0
