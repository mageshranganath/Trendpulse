@echo off
setlocal

cd /d "%~dp0"

set "ADB_CMD=adb"
where adb >nul 2>nul
if errorlevel 1 (
  if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "ADB_CMD=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
  if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "ADB_CMD=%ANDROID_HOME%\platform-tools\adb.exe"
  if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" set "ADB_CMD=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
)

echo ==========================================
echo CurrencyTrend: build + install + launch
echo ==========================================
echo.

echo [1/4] Checking connected Android devices...
"%ADB_CMD%" devices
if errorlevel 1 (
  echo WARNING: adb not found from PATH or common SDK locations.
  echo Build and install can still run via Gradle, but auto-launch may fail.
)
echo.

echo [2/4] Building debug APK...
call gradlew.bat :app:assembleDebug
if errorlevel 1 goto :fail
echo.

echo [3/4] Installing debug APK on device...
call gradlew.bat :app:installDebug
if errorlevel 1 goto :install_fail
echo.

echo [4/4] Launching app...
"%ADB_CMD%" shell am start -n com.currencytrend.app/.MainActivity
if errorlevel 1 (
  echo App installed but could not auto-launch. Open it manually from your phone.
  goto :ok
)

:ok
echo.
echo SUCCESS: App built, installed, and launched.
exit /b 0

:install_fail
echo.
echo INSTALL FAILED: No target device/emulator found or USB debugging not authorized.
echo Connect your phone and accept the USB debugging prompt, then run again.
exit /b 1

:fail
echo.
echo BUILD FAILED: Please check Gradle errors above.
exit /b 1
