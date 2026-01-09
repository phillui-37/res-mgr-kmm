Write-Host "Building Client (Windows)..."
.\gradlew.bat composeApp:packageDistributionForCurrentOS

# Prepare release directory
New-Item -ItemType Directory -Force -Path release\client | Out-Null

# Copy
Copy-Item -Recurse -Force composeApp\build\compose\binaries\main\* release\client\

Write-Host "Client build complete. Artifacts in release\client\"

