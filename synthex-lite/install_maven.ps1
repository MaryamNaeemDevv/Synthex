$mavenVersion = "3.9.6"
$downloadUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$installDir = "C:\apache-maven"
$zipPath = "$env:TEMP\maven.zip"

Write-Host "--- Synthex Maven Installer ---" -ForegroundColor Cyan

# 1. Create Install Directory
if (!(Test-Path $installDir)) {
    Write-Host "Creating directory $installDir..."
    New-Item -ItemType Directory -Path $installDir -Force | Out-Null
}

# 2. Download Maven
Write-Host "Downloading Maven $mavenVersion from $downloadUrl..." -ForegroundColor Yellow
Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath

# 3. Extract Maven
Write-Host "Extracting to $installDir..." -ForegroundColor Yellow
Expand-Archive -Path $zipPath -DestinationPath $installDir -Force

# 4. Get the actual bin path (handling the nested folder in the zip)
$extractedFolder = Get-ChildItem -Path $installDir -Directory | Select-Object -First 1
$binPath = Join-Path $extractedFolder.FullName "bin"

# 5. Add to User Path permanently
Write-Host "Adding $binPath to User PATH..." -ForegroundColor Green
$oldPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
if ($oldPath -notlike "*$binPath*") {
    $newPath = "$oldPath;$binPath"
    [System.Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "PATH updated successfully!" -ForegroundColor Green
} else {
    Write-Host "Maven is already in PATH." -ForegroundColor Cyan
}

# 6. Cleanup
Remove-Item $zipPath -ErrorAction SilentlyContinue

Write-Host "`nInstallation Complete!" -ForegroundColor Cyan
Write-Host "IMPORTANT: Please CLOSE this terminal and open a NEW one for the changes to take effect." -ForegroundColor Yellow
Write-Host "Then run: mvn spring-boot:run" -ForegroundColor White
