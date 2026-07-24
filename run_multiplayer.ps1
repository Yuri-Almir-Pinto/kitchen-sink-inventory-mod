param (
    [string]$Loader = "fabric" # Defaults to fabric, pass 'neoforge' if needed
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Starting $Loader Server + 2 Clients " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\gradlew.bat :$Loader`:runServer"

Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\gradlew.bat :$Loader`:runClient --args='--username Timao'"

Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\gradlew.bat :$Loader`:runClient --args='--username Pumba'"

Write-Host "All tasks dispatched!" -ForegroundColor Green