@echo off
REM Backend Connection Test Script
REM Run this to verify backend is accessible before running Android app

echo ========================================
echo Backend Connection Test
echo ========================================
echo.

echo [1/4] Testing if Python backend is running...
curl -s http://localhost:8000/health
if %errorlevel% neq 0 (
    echo FAILED: Backend is not responding
    echo Please start the backend with: python run_api.py
    goto :end
) else (
    echo SUCCESS: Backend is running
)
echo.

echo [2/4] Testing game motor baseline endpoint...
curl -s http://localhost:8000/game/motor-baseline/test123
echo.
echo SUCCESS: Game endpoint is accessible
echo.

echo [3/4] Testing from emulator perspective (10.0.2.2)...
curl -s http://127.0.0.1:8000/health
if %errorlevel% neq 0 (
    echo WARNING: Localhost address not working
) else (
    echo SUCCESS: Emulator should be able to reach backend
)
echo.

echo [4/4] Checking if backend is listening on all interfaces...
netstat -ano | findstr :8000
echo.

echo ========================================
echo Test Complete
echo ========================================
echo.
echo Next steps:
echo 1. If all tests passed, rebuild your Android app
echo 2. Check Windows Firewall if connection still fails
echo 3. Check Android Logcat for detailed error messages
echo.
echo To disable Windows Firewall temporarily (as Admin):
echo   netsh advfirewall set allprofiles state off
echo.
echo To enable it again (as Admin):
echo   netsh advfirewall set allprofiles state on
echo.

:end
pause
