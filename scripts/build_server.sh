#!/usr/bin/env bash

# Build Server Distribution
echo "Building Server..."
./gradlew server:installDist

# Prepare release directory
mkdir -p release/server

# Copy
cp -r server/build/install/server/* release/server/

echo "Server build complete. Artifacts in release/server/"

