#!/usr/bin/env bash

# Build Client Distribution
echo "Building Client (Current OS)..."
./gradlew composeApp:packageDistributionForCurrentOS

# Prepare release directory
mkdir -p release/client

# Copy (Path depends on OS, usually under binaries/main)
# We copy the whole binaries folder structure to be safe or try to find the specific output
cp -r composeApp/build/compose/binaries/main/* release/client/

echo "Client build complete. Artifacts in release/client/"

