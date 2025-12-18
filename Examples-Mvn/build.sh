#!/bin/bash

# Conditional build script
# Usage:
#   ./build.sh                    - Only build the project
#   ./build.sh start              - Build and start the application
#   ./build.sh -ffmpegUtils       - Build project with FFmpeg utils
#   ./build.sh -mediaUtils        - Build project with Media utils
#   ./build.sh -native            - Build project with all native libraries
#   ./build.sh -native start      - Build project with native libraries and start
#   ./build.sh cli <jsonFileName> - Run CLI mode with JSON config file
#   ./build.sh cli <basicClassName> - Run CLI mode with basic class name
#   ./build.sh -m start           - Build and start with memory leak detection (tcmalloc)
#   ./build.sh -m cli <config>    - Run CLI mode with memory leak detection (tcmalloc)

set -e

# Initialize flags
build_native=false
build_ffmpeg=false
build_media=false
enable_memleak=false
action="compile"
cli_config=""

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
    -m)
        enable_memleak=true
        shift
        ;;
    start)
        action="start"
        shift
        ;;
    cli)
        action="cli"
        shift
        if [ $# -gt 0 ] && [ "$1" != "-m" ]; then
            cli_config="$1"
            shift
        else
            cli_config=""
        fi
        ;;
    *)
        echo "Unknown option: $1"
        echo "Usage: $0 [-native|-ffmpegUtils|-mediaUtils|-m] [start|cli <config>]"
        exit 1
        ;;
    esac
done

# Function to setup tcmalloc memory leak detection
setup_memleak_detection() {
    local log_dir="$1"
    
    # Clean and recreate log directory
    if [ -d "$log_dir" ]; then
        echo "Cleaning existing memory leak log directory: $log_dir"
        rm -rf "$log_dir"
    fi
    echo "Creating memory leak log directory: $log_dir"
    mkdir -p "$log_dir"
    
    # Find tcmalloc library
    TCMALLOC_LIB=""
    for lib_path in "/usr/lib/x86_64-linux-gnu/libtcmalloc.so" \
                    "/usr/lib/x86_64-linux-gnu/libtcmalloc.so.4" \
                    "/usr/lib/libtcmalloc.so" \
                    "/usr/local/lib/libtcmalloc.so"; do
        if [ -f "$lib_path" ]; then
            TCMALLOC_LIB="$lib_path"
            break
        fi
    done
    
    if [ -z "$TCMALLOC_LIB" ]; then
        echo "❌ Error: tcmalloc library not found!"
        echo "Please install tcmalloc:"
        echo "   sudo apt-get install libgoogle-perftools-dev"
        echo ""
        echo "After installation, tcmalloc will be available at:"
        echo "   /usr/lib/x86_64-linux-gnu/libtcmalloc.so"
        exit 1
    fi
    
    echo "✅ Found tcmalloc: $TCMALLOC_LIB"
    echo "📁 Memory leak log directory: $log_dir"
    
    # Set tcmalloc environment variables
    export LD_PRELOAD="$TCMALLOC_LIB"
    export HEAPCHECK=normal  # Options: minimal, normal, strict, draconian
    export HEAP_CHECK_DUMP_DIRECTORY="$log_dir"
    export PPROF_PATH=$(which pprof 2>/dev/null || which google-pprof 2>/dev/null || echo "/usr/bin/pprof")
    
    # Configure tcmalloc to be compatible with JVM and Spring Boot
    export HEAPCHECK_IGNORE_GLOBAL_LIVE=1      # Ignore global/static allocations
    export HEAP_CHECK_IDENTIFY_LEAKS=1         # Only report definite leaks
    export HEAP_CHECK_AFTER_DESTRUCTORS=0      # Don't check after static destructors
    
    # Don't use HEAP_CHECK_TEST_POINTER_ALIGNMENT - it causes hangs with Spring Boot Loader
}

# Function to analyze tcmalloc leak report using pprof
analyze_leak_report() {
    local exit_code=$1
    local log_file="$2"
    
    echo ""
    echo "=============================================="
    echo "🔍 Analyzing Memory Leak Report with pprof"
    echo "=============================================="
    echo ""
    
    # Check if there were any leaks
    if [ $exit_code -eq 0 ]; then
        echo "✅ SUCCESS: No memory leaks detected!"
        echo ""
        return 0
    fi
    
    # Find the heap dump file
    local dump_dir=$(dirname "$log_file")
    local heap_file=$(ls -t "$dump_dir"/*.heap 2>/dev/null | head -1)
    
    if [ -z "$heap_file" ]; then
        echo "❌ No heap dump file found in $dump_dir"
        echo "Falling back to log file analysis..."
        echo ""
        grep "Leak of" "$log_file" | head -20
        return 1
    fi
    
    echo "📁 Heap dump file: $heap_file"
    echo ""
    
    # Check if pprof is available (Go version required)
    if ! command -v pprof &> /dev/null; then
        echo "❌ pprof not found. Please install it first."
        echo ""
        echo "=============================================="
        echo "📦 Installation Guide:"
        echo "=============================================="
        echo ""
        echo "1️⃣  Check if Go is installed:"
        echo "   go version"
        echo ""
        echo "2️⃣  If Go is not installed, install it:"
        echo "   sudo apt-get update"
        echo "   sudo apt-get install golang-go"
        echo ""
        echo "3️⃣  Install pprof using Go (with China mirror):"
        echo "   export GOPROXY=https://goproxy.cn,direct"
        echo "   go install github.com/google/pprof@latest"
        echo ""
        echo "4️⃣  Add pprof to PATH:"
        echo "   echo 'export PATH=\$PATH:\$HOME/go/bin' >> ~/.bashrc"
        echo "   source ~/.bashrc"
        echo ""
        echo "5️⃣  Verify installation:"
        echo "   pprof -h"
        echo ""
        echo "=============================================="
        echo ""
        return 1
    fi
    
    local PPROF_CMD="pprof"
    echo "✅ Using pprof: $(which pprof)"
    
    echo ""
    
    # Find java binary
    local JAVA_BIN=$(which java)
    
    # Run pprof analysis with different views
    # echo "📊 Running pprof analysis..."
    # echo "=============================================="
    # echo ""
    
    # echo "📋 1. Summary (Top allocations):"
    # echo "--------------------------------------------"
    # $PPROF_CMD -text -lines "$JAVA_BIN" "$heap_file"
    # echo ""
    
    echo "📋 2. Complete list (All allocations):"
    echo "--------------------------------------------"
    # Use -nodefraction=0 to show all nodes, not just the top ones
    $PPROF_CMD -text -lines -nodefraction=0 -edgefraction=0 "$JAVA_BIN" "$heap_file"
    echo ""
    
    echo "📋 3. SDK Memory Leaks (Agora/RTE only):"
    echo "--------------------------------------------"
    # Filter and show only lines containing 'agora' or 'rte' (case-insensitive)
    local sdk_leaks=$($PPROF_CMD -text -lines -nodefraction=0 -edgefraction=0 "$JAVA_BIN" "$heap_file" | grep -iE '(agora|rte)')
    
    if [ -z "$sdk_leaks" ]; then
        echo "✅ No SDK-related memory leaks detected (no 'agora' or 'rte' in allocations)"
    else
        echo "⚠️  Found SDK-related allocations:"
        echo ""
        echo "$sdk_leaks"
        echo ""
        
        # Count the number of SDK-related leaks
        local leak_count=$(echo "$sdk_leaks" | wc -l)
        echo "📊 Total SDK-related allocation entries: $leak_count"
        
        # Calculate total leaked bytes from SDK
        local total_bytes=$(echo "$sdk_leaks" | awk '{if ($1 ~ /^[0-9]+$/) sum += $1} END {print sum}')
        if [ -n "$total_bytes" ] && [ "$total_bytes" -gt 0 ]; then
            echo "💾 Total SDK-related memory: $total_bytes bytes"
        fi
    fi
    echo ""
    
    # echo "📋 3. Call graph (Detailed):"
    # echo "--------------------------------------------"
    # $PPROF_CMD -tree -lines "$JAVA_BIN" "$heap_file" | head -100
    # echo ""

}

echo ""
echo "=== Preparing build environment ==="
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
        
        # Setup memory leak detection if enabled
        if [ "$enable_memleak" = "true" ]; then
            DUMP_DIR="$(pwd)/logs/memleak"
            setup_memleak_detection "$DUMP_DIR"
            LOG_FILE="$DUMP_DIR/app.log"
            echo "Memory leak detection enabled. Log file: $LOG_FILE"
            
            # Use -Xint to disable JIT compilation and avoid crashes with tcmalloc
            java -Xint -Dserver.port=18080 -jar target/agora-example.jar 2>&1 | tee "$LOG_FILE"
            APP_EXIT_CODE=${PIPESTATUS[0]}
            analyze_leak_report $APP_EXIT_CODE "$LOG_FILE"
        else
            java -Dserver.port=18080 -jar target/agora-example.jar
        fi
        
    elif [ "$action" = "cli" ]; then
        echo "=== Run CLI mode ==="
        
        if [ -z "$cli_config" ]; then
            echo "No task specified, will run all basic test cases..."
        fi

        # Get absolute paths
        PROJECT_DIR=$(pwd)
        SYS_LIB="/usr/lib/x86_64-linux-gnu"
        APP_LIB="$PROJECT_DIR/libs/native/linux/x86_64"
        THIRD_PARTY_LIB="$PROJECT_DIR/third_party"
        
        # Check if library directories exist
        if [ ! -d "$APP_LIB" ]; then
            echo "Warning: Library directory not found: $APP_LIB"
            echo "Please ensure native libraries are extracted or built."
        fi
        if [ ! -d "$THIRD_PARTY_LIB" ]; then
            echo "Warning: Third party library directory not found: $THIRD_PARTY_LIB"
        fi

        # Create logs directory if it doesn't exist
        LOGS_DIR="$PROJECT_DIR/logs/agora_logs"
        if [ ! -d "$LOGS_DIR" ]; then
            echo "Creating logs directory: $LOGS_DIR"
            mkdir -p "$LOGS_DIR"
        fi

        echo "Setting LD_LIBRARY_PATH..."
        export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$SYS_LIB:$APP_LIB:$THIRD_PARTY_LIB"
        echo "LD_LIBRARY_PATH = $LD_LIBRARY_PATH"
        
        # Extract JAR if not already extracted to avoid Spring Boot Loader issues with native threads
        # Do this BEFORE enabling tcmalloc to avoid false positives from jar command
        EXTRACTED_DIR="$PROJECT_DIR/target/extracted"
        if [ ! -d "$EXTRACTED_DIR" ]; then
            echo "Extracting JAR to avoid Spring Boot Loader..."
            mkdir -p "$EXTRACTED_DIR"
            cd "$EXTRACTED_DIR"
            jar -xf ../agora-example.jar
            cd "$PROJECT_DIR"
        fi
        
        # Build classpath from extracted JAR
        CLASSPATH="$EXTRACTED_DIR/BOOT-INF/classes"
        for jar in "$EXTRACTED_DIR"/BOOT-INF/lib/*.jar; do
            CLASSPATH="$CLASSPATH:$jar"
        done
        
        # Prepare task argument
        if [ -z "$cli_config" ]; then
            TASK_ARG=""
            echo "Running: java -cp $CLASSPATH io.agora.rtc.example.cli.CliLauncher (no task - will run all basic tests)"
        else
            TASK_ARG="--task=$cli_config"
            echo "Running: java -cp $CLASSPATH io.agora.rtc.example.cli.CliLauncher --task=$cli_config"
        fi
        
        # Setup memory leak detection if enabled (AFTER jar extraction)
        if [ "$enable_memleak" = "true" ]; then
            DUMP_DIR="$PROJECT_DIR/logs/memleak"
            setup_memleak_detection "$DUMP_DIR"
            LOG_FILE="$DUMP_DIR/app.log"
            echo "Memory leak detection enabled. Log file: $LOG_FILE"
            
            # Disable exit on error temporarily to capture exit code
            set +e
            # Use -Xint to disable JIT compilation and avoid crashes with tcmalloc
            java -Xint -cp "$CLASSPATH" io.agora.rtc.example.cli.CliLauncher $TASK_ARG 2>&1 | tee "$LOG_FILE"
            APP_EXIT_CODE=${PIPESTATUS[0]}
            set -e
            
            analyze_leak_report $APP_EXIT_CODE "$LOG_FILE"
        else
            java -cp "$CLASSPATH" io.agora.rtc.example.cli.CliLauncher $TASK_ARG
        fi
        
    else
        echo "Build finished! To start the application, use: ./build.sh start"
        echo "To run CLI mode:"
        echo "  - Run all basic tests: ./build.sh cli"
        echo "  - Run specific test: ./build.sh cli <jsonFileName> or ./build.sh cli <basicClassName>"
        echo "To enable memory leak detection, add -m flag:"
        echo "  - ./build.sh -m start"
        echo "  - ./build.sh cli -m"
        echo "  - ./build.sh cli SendH264Test -m"
    fi

else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi
