$USERNAME = "henryngyn2264"

$services = @(
    "config-server", "eureka-server", "api-gateway",
    "user-service", "verification-service", "vehicle-service",
    "wallet-service", "media-service", "market-service", "transaction-service"
)

foreach ($service in $services) {
    Write-Host "Building $service..." -ForegroundColor Yellow
    docker build -t "$USERNAME/$service`:latest" "./$service"

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Pushing $service..." -ForegroundColor Green
        docker push "$USERNAME/$service`:latest"
    } else {
        Write-Host "Build failed for $service!" -ForegroundColor Red
    }
}

Write-Host "All done!" -ForegroundColor Cyan
