Write-Host "Building Server..."
.\gradlew.bat server:installDist

# Prepare release directory
New-Item -ItemType Directory -Force -Path release\server | Out-Null

# Copy
Copy-Item -Recurse -Force server\build\install\server\* release\server\

Write-Host "Server build complete. Artifacts in release\server\"

