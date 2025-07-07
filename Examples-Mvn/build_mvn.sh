#!/bin/bash

# Conditional build script
# Usage:
#   ./build_mvn.sh        - Only build the project
#   ./build_mvn.sh start  - Build and start the application

set -e

# Check arguments
ACTION="${1:-compile}"

# Config file path
CONFIG_FILE="run_config"
AUDIO3A_FILE="src/main/java/io/agora/rtc/example/basic/Audio3aTest.java"
VADV1_FILE="src/main/java/io/agora/rtc/example/basic/VadV1Test.java"

# Default config
enable_aed_vad="false"
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
        "enable_aed_vad") enable_aed_vad="$value" ;;
        "enable_gateway") enable_gateway="$value" ;;
        esac
    done <"$CONFIG_FILE"

    echo "Config options:"
    echo "  enable_aed_vad = $enable_aed_vad"
    echo "  enable_gateway = $enable_gateway"
fi

echo ""
echo "=== Preparing build environment ==="

# Enable/disable files according to config
if [ "$enable_aed_vad" = "false" ]; then
    if [ -f "$VADV1_FILE" ]; then
        echo "❌ Disable VadV1Test.java (enable_aed_vad=false)"
        mv "$VADV1_FILE" "$VADV1_FILE.disabled"
    fi
else
    if [ -f "$VADV1_FILE.disabled" ]; then
        echo "✅ Enable VadV1Test.java (enable_aed_vad=true)"
        mv "$VADV1_FILE.disabled" "$VADV1_FILE"
    fi
fi

if [ "$enable_gateway" = "false" ]; then
    if [ -f "$AUDIO3A_FILE" ]; then
        echo "❌ Disable Audio3aTest.java (enable_gateway=false)"
        mv "$AUDIO3A_FILE" "$AUDIO3A_FILE.disabled"
    fi
else
    if [ -f "$AUDIO3A_FILE.disabled" ]; then
        echo "✅ Enable Audio3aTest.java (enable_gateway=true)"
        mv "$AUDIO3A_FILE.disabled" "$AUDIO3A_FILE"
    fi
fi

echo ""
echo "=== Start building ==="
mvn clean package

# Check build result
if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Build succeeded!"
    echo ""

    # Start application if ACTION is "start"
    if [ "$ACTION" = "start" ]; then
        echo "=== Clean port and start application ==="

        # Check if port 18080 is occupied
        echo "Checking port 18080..."
        PORT_PID=$(sudo lsof -ti :18080)
        if [ -n "$PORT_PID" ]; then
            echo "Port 18080 is occupied by process $PORT_PID, killing..."
            sudo lsof -ti :18080 | xargs -r sudo kill -9
            sleep 2
            echo "Port cleaned"
        else
            echo "Port 18080 is not occupied"
        fi

        # Set environment variable and start application
        echo "Starting application..."
        echo "Setting LD_LIBRARY_PATH..."
        export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:libs/native/linux/x86_64:third_party"
        echo "LD_LIBRARY_PATH = $LD_LIBRARY_PATH"

        echo "Starting agora-example.jar on port 18080..."
        java -Dserver.port=18080 -jar target/agora-example.jar
    else
        echo "Build finished! To start the application, use: ./build_mvn.sh start"
    fi

else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi
