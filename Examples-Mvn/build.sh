#!/bin/bash

# Conditional build script
# Usage:
#   ./build.sh                    - Only build the project
#   ./build.sh start              - Build and start the application
#   ./build.sh -ffmpegUtils       - Build project with FFmpeg utils
#   ./build.sh -mediaUtils        - Build project with Media utils
#   ./build.sh -native            - Build project with all native libraries
#   ./build.sh -native start      - Build project with native libraries and start

set -e

# Initialize flags
build_native=false
build_ffmpeg=false
build_media=false
action="compile"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
    -native)
        build_native=true
        shift
        ;;
    -ffmpegUtils)
        build_ffmpeg=true
        shift
        ;;
    -mediaUtils)
        build_media=true
        shift
        ;;
    start)
        action="start"
        shift
        ;;
    *)
        echo "Unknown option: $1"
        echo "Usage: $0 [-native|-ffmpegUtils|-mediaUtils] [start]"
        exit 1
        ;;
    esac
done

# Config file path
CONFIG_FILE="run_config"
AUDIO3A_FILE="src/main/java/io/agora/rtc/example/basic/Audio3aTest.java"
VADV1_FILE="src/main/java/io/agora/rtc/example/basic/VadV1Test.java"

# Default config
enable_gateway="false"

# Read config
if [ -f "$CONFIG_FILE" ]; then
    echo "Reading config file: $CONFIG_FILE"
    while IFS='=' read -r key value; do
        if [[ -z "$key" || "$key" =~ ^#.* ]]; then
            continue
        fi
        key=$(echo "$key" | xargs)
        value=$(echo "$value" | xargs)
        case "$key" in
        "enable_gateway") enable_gateway="$value" ;;
        esac
    done <"$CONFIG_FILE"

    echo "Config options:"
    echo "  enable_gateway = $enable_gateway"
fi

echo ""
echo "=== Preparing build environment ==="

# Enable/disable files according to config
if [ "$enable_gateway" = "false" ]; then
    if [ -f "$VADV1_FILE" ]; then
        echo "❌ Disable VadV1Test.java (enable_gateway=false)"
        mv "$VADV1_FILE" "$VADV1_FILE.disabled"
    fi
    if [ -f "$AUDIO3A_FILE" ]; then
        echo "❌ Disable Audio3aTest.java (enable_gateway=false)"
        mv "$AUDIO3A_FILE" "$AUDIO3A_FILE.disabled"
    fi
else
    if [ -f "$VADV1_FILE.disabled" ]; then
        echo "✅ Enable VadV1Test.java (enable_gateway=true)"
        mv "$VADV1_FILE.disabled" "$VADV1_FILE"
    fi
    if [ -f "$AUDIO3A_FILE.disabled" ]; then
        echo "✅ Enable Audio3aTest.java (enable_gateway=true)"
        mv "$AUDIO3A_FILE.disabled" "$AUDIO3A_FILE"
    fi
fi

echo ""
echo "=== Start building ==="

# Build native libraries if requested
if [ "$build_native" = "true" ]; then
    echo "Building all native libraries..."
    mvn clean package -Dbuild.native=true
elif [ "$build_ffmpeg" = "true" ]; then
    echo "Building FFmpeg utils..."
    mvn clean package -Dbuild.ffmpeg=true
elif [ "$build_media" = "true" ]; then
    echo "Building Media utils..."
    mvn clean package -Dbuild.media=true
else
    echo "Building without native libraries..."
    mvn clean package
fi

# Check build result
if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Build succeeded!"
    echo ""

    # Start application if action is "start"
    if [ "$action" = "start" ]; then
        echo "=== Clean port and start application ==="

        # Check if port 18080 is occupied using lsof
        echo "Checking port 18080..."
        if lsof -t -i:18080 2>/dev/null | read -r; then
            echo "Port 18080 is occupied, attempting to kill..."
            # Use xargs to handle multiple PIDs
            lsof -t -i:18080 2>/dev/null | xargs kill -9 2>/dev/null || {
                echo "Failed to kill process(es) with regular user, trying with sudo..."
                lsof -t -i:18080 2>/dev/null | xargs sudo kill -9 2>/dev/null || echo "Failed to kill process(es), continuing..."
            }
            sleep 2 # Give OS time to release the port
            echo "Port cleaned"
        else
            echo "Port 18080 is not occupied"
        fi

        # Set environment variable and start application
        echo "Starting application..."
        echo "Setting LD_LIBRARY_PATH..."
        export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/usr/lib/x86_64-linux-gnu:libs/native/linux/x86_64:third_party"
        echo "LD_LIBRARY_PATH = $LD_LIBRARY_PATH"

        echo "Starting agora-example.jar on port 18080..."
        java -Dserver.port=18080 -jar target/agora-example.jar
    else
        echo "Build finished! To start the application, use: ./build.sh start"
    fi

else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi
