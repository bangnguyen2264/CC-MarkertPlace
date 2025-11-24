# ===============================================
# deploy.ps1 - Auto Build + Versioning + Push + Update docker-compose
# Thư mục gốc: D:\java tutor\cc-marketplace-docker
# ===============================================

$USERNAME = "henryngyn2264"
$ROOT_PATH = "D:\java tutor\cc-marketplace-docker"
$COMPOSE_FILE = "$ROOT_PATH\docker-compose.yml"
$VERSION_FILE = "$ROOT_PATH\.build-version"

# Danh sách các service cần build
$services = @(
    "config-server", "eureka-server", "api-gateway",
    "user-service", "verification-service", "vehicle-service",
    "wallet-service", "media-service", "market-service", "transaction-service"
)

# Đọc hoặc khởi tạo version (format: major.minor.patch-build)
function Get-CurrentVersion {
    if (Test-Path $VERSION_FILE) {
        return Get-Content $VERSION_FILE -Raw | Trim
    } else {
        return "1.0.3"
    }
}

function Save-Version($version) {
    $version | Out-File -FilePath $VERSION_FILE -Encoding UTF8
}

# Tăng build number (1.0.0-1 → 1.0.0-2)
function Increment-Version($current) {
    if ($current -match '(\d+\.\d+\.\d+)-(\d+)') {
        $base = $matches[1]
        $build = [int]$matches[2] + 1
        return "$base-$build"
    } else {
        return "1.1.0"
    }
}

# Đọc version hiện tại và tăng lên
$currentVersion = Get-CurrentVersion
$newVersion = Increment-Version $currentVersion
Write-Host "Building version: $newVersion" -ForegroundColor Magenta

# Build + Push tất cả service với version mới
foreach ($service in $services) {
    $imageWithVersion = "$USERNAME/$service`:$newVersion"
    $imageLatest = "$USERNAME/$service`:latest"

    Write-Host "`nBuilding $service v$newVersion ..." -ForegroundColor Yellow

    docker build -t $imageWithVersion -t $imageLatest "./$service"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed for $service!" -ForegroundColor Red
        exit 1
    }

    Write-Host "Pushing $imageWithVersion ..." -ForegroundColor Green
    docker push $imageWithVersion
    docker push $imageLatest

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Push failed for $service!" -ForegroundColor Red
        exit 1
    }

    Write-Host "$service v$newVersion pushed successfully!" -ForegroundColor Cyan
}

# Cập nhật docker-compose.yml để dùng đúng version mới
Write-Host "`nUpdating docker-compose.yml to use version $newVersion ..." -ForegroundColor Yellow

# Đọc file compose
$content = Get-Content $COMPOSE_FILE -Raw

# Thay thế tất cả image: username/service:latest → username/service:<newVersion>
foreach ($service in $services) {
    $oldImage = "$USERNAME/$service`:latest"
    $newImage = "$USERNAME/$service`:$newVersion"
    $content = $content -replace [regex]::Escape($oldImage), $newImage
}

# Ghi lại file
$content | Set-Content -Path $COMPOSE_FILE -Encoding UTF8

# Lưu version mới để lần sau tăng tiếp
Save-Version $newVersion

Write-Host "`nAll services built and pushed as version: $newVersion" -ForegroundColor Magenta
Write-Host "docker-compose.yml has been updated to use version: $newVersion" -ForegroundColor Green
Write-Host "File version saved to: .build-version" -ForegroundColor Gray

Write-Host "`nYou can now run:" -ForegroundColor Cyan
Write-Host "   docker-compose up -d" -ForegroundColor White
Write-Host "   docker-compose pull && docker-compose up -d" -ForegroundColor White

Write-Host "`nDeployment completed successfully!" -ForegroundColor Cyan