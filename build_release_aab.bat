@echo off
setlocal
cd /d "%~dp0"

echo Building signed release AAB...
call gradlew.bat :app:bundleRelease
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)

echo.
echo Done. Output:
echo app\build\outputs\bundle\release\app-release.aab
exit /b 0
