# Add Windows Firewall Rule for Backend API
# Run this script as Administrator

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Windows Firewall Configuration" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "ERROR: This script must be run as Administrator!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Right-click on PowerShell and select 'Run as Administrator'" -ForegroundColor Yellow
    Write-Host ""
    pause
    exit 1
}

Write-Host "✓ Running as Administrator" -ForegroundColor Green
Write-Host ""

# Check if rule already exists
$existingRule = Get-NetFirewallRule -DisplayName "Python Backend API (Port 8000)" -ErrorAction SilentlyContinue

if ($existingRule) {
    Write-Host "Rule already exists. Removing old rule..." -ForegroundColor Yellow
    Remove-NetFirewallRule -DisplayName "Python Backend API (Port 8000)"
}

# Add new firewall rule
Write-Host "Adding firewall rule for port 8000..." -ForegroundColor Yellow

try {
    New-NetFirewallRule `
        -DisplayName "Python Backend API (Port 8000)" `
        -Direction Inbound `
        -Protocol TCP `
        -LocalPort 8000 `
        -Action Allow `
        -Profile Any `
        -Description "Allow Python backend API connections from Android emulator and local network"
    
    Write-Host ""
    Write-Host "✓ Firewall rule added successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Port 8000 is now accessible from:" -ForegroundColor Cyan
    Write-Host "  • Android Emulator (10.0.2.2:8000)" -ForegroundColor White
    Write-Host "  • Local Network (192.168.x.x:8000)" -ForegroundColor White
    Write-Host "  • Localhost (127.0.0.1:8000)" -ForegroundColor White
    Write-Host ""
    
} catch {
    Write-Host ""
    Write-Host "✗ Failed to add firewall rule!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# Verify the rule
Write-Host "Verifying firewall rule..." -ForegroundColor Yellow
$rule = Get-NetFirewallRule -DisplayName "Python Backend API (Port 8000)"

if ($rule) {
    Write-Host "✓ Rule verified:" -ForegroundColor Green
    Write-Host "  Name: $($rule.DisplayName)" -ForegroundColor White
    Write-Host "  Enabled: $($rule.Enabled)" -ForegroundColor White
    Write-Host "  Action: $($rule.Action)" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "✗ Could not verify rule" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Configuration Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Rebuild your Android app" -ForegroundColor White
Write-Host "2. Run the app in emulator" -ForegroundColor White
Write-Host "3. Check if connection succeeds" -ForegroundColor White
Write-Host ""
Write-Host "If you still have issues:" -ForegroundColor Yellow
Write-Host "• Check backend is running: python run_api.py" -ForegroundColor White
Write-Host "• Check Android Logcat for errors" -ForegroundColor White
Write-Host "• Run test_backend_connection.bat" -ForegroundColor White
Write-Host ""

pause
