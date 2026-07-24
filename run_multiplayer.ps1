param (
    [string]$Loader = "fabric" # Defaults to fabric, pass 'neoforge' if needed
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Starting $Loader Server + 2 Clients " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Launch Server in its own new PowerShell window
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\gradlew.bat :$Loader`:runServer"

# 2. Wait 5 seconds for server setup
Start-Sleep -Seconds 5

# 3. Launch Client 1 (Player1)
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\gradlew.bat :$Loader`:runClient --args='--username Player1'"

# 4. Launch Client 2 (Player2)
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\gradlew.bat :$Loader`:runClient --args='--username Player2'"

Write-Host "All tasks dispatched!" -ForegroundColor Green