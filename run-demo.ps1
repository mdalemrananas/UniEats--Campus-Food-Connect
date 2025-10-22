# Real-Time Stock Update Demo Runner
# This script helps you run the server and multiple clients easily

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('server', 'client', 'help')]
    [string]$Mode = 'help'
)

$PROJECT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$SERVER_CLASS = "com.unieats.demo.RealTimeStockServer"
$CLIENT_CLASS = "com.unieats.demo.RealTimeStockDemoClient"

function Show-Help {
    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host "  Real-Time Stock Update Demo - Runner Script" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host "`nUsage:" -ForegroundColor Yellow
    Write-Host "  .\run-demo.ps1 server   # Start the WebSocket server" -ForegroundColor White
    Write-Host "  .\run-demo.ps1 client   # Start a JavaFX client" -ForegroundColor White
    Write-Host "`nSteps to run the demo:" -ForegroundColor Yellow
    Write-Host "  1. Open first terminal and run:  .\run-demo.ps1 server" -ForegroundColor White
    Write-Host "  2. Wait for server to start" -ForegroundColor White
    Write-Host "  3. Open second terminal and run:  .\run-demo.ps1 client" -ForegroundColor White
    Write-Host "  4. Open third terminal and run:   .\run-demo.ps1 client" -ForegroundColor White
    Write-Host "  5. Click 'Buy Now' in one client and watch the other update!" -ForegroundColor White
    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Start-Server {
    Write-Host "`n==================================================================" -ForegroundColor Green
    Write-Host "  Starting Real-Time Stock Server..." -ForegroundColor Green
    Write-Host "==================================================================" -ForegroundColor Green
    Write-Host ""
    
    Set-Location $PROJECT_DIR
    
    # Compile if needed
    Write-Host "Compiling project..." -ForegroundColor Yellow
    mvn clean compile -q
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`nERROR: Compilation failed!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Starting server...`n" -ForegroundColor Yellow
    
    # Run server
    mvn exec:java -Dexec.mainClass="$SERVER_CLASS" -Dexec.cleanupDaemonThreads=false
}

function Start-Client {
    Write-Host "`n==================================================================" -ForegroundColor Blue
    Write-Host "  Starting Real-Time Stock Client..." -ForegroundColor Blue
    Write-Host "==================================================================" -ForegroundColor Blue
    Write-Host ""
    
    Set-Location $PROJECT_DIR
    
    # Compile if needed
    Write-Host "Compiling project..." -ForegroundColor Yellow
    mvn clean compile -q
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`nERROR: Compilation failed!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Starting client...`n" -ForegroundColor Yellow
    
    # Run client with JavaFX
    mvn javafx:run -Djavafx.mainClass="$CLIENT_CLASS"
}

# Main execution
switch ($Mode) {
    'server' {
        Start-Server
    }
    'client' {
        Start-Client
    }
    'help' {
        Show-Help
    }
    default {
        Show-Help
    }
}
