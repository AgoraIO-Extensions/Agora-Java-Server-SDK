#!/usr/bin/env zsh
#sh run.zsh io.agora.rtc.example.test.H264PcmTest -channelId  aga -token 123 -sampleRate 23900

ARCH=$(uname -m)

LIB_PATH="libs/native/linux/$ARCH"

export DYLD_LIBRARY_PATH=$(pwd)/libs:$(pwd)/$LIB_PATH:$(pwd)/third_party
export LD_LIBRARY_PATH=$(pwd)/libs:$(pwd)/$LIB_PATH:$(pwd)/third_party
echo "LD_LIBRARY_PATH: $LD_LIBRARY_PATH"

CLASS=$1
shift 1

echo "Parameters passed to run.zsh:$CLASS $@"

# Read configuration file
CONFIG_FILE="run_config"
# Default: JNI checks are disabled (enable_jni_check=false)
ENABLE_JNI_CHECK=false
# Default: ASAN is disabled
ENABLE_ASAN=false
ENABLE_VAD1=false

if [ -f "$CONFIG_FILE" ]; then
    echo "Reading configuration from $CONFIG_FILE"
    # Read settings from the configuration file
    while IFS='=' read -r key value; do
        if [ "$key" = "enable_jni_check" ]; then
            ENABLE_JNI_CHECK=$value
        elif [ "$key" = "enable_asan" ]; then
            ENABLE_ASAN=$value
        elif [ "$key" = "enable_vad1" ]; then
            ENABLE_VAD1=$value
        fi
        # You can read more configuration items here
    done <"$CONFIG_FILE"
fi

# Set JNI check options
if [ "$ENABLE_JNI_CHECK" = "false" ]; then
    echo "JNI checks are disabled (default mode)"
    JNI_OPTS="-XX:-CheckJNICalls"
else
    echo "JNI checks are enabled (debug/dev mode)"
    JNI_OPTS="-Xcheck:jni"
fi

if [ "$ENABLE_VAD1" = "false" ]; then
    echo "VAD1 is disabled"
    if [[ "$CLASS" == *"VadV1Test"* ]]; then
        echo "Error: Cannot run VadV1Test when VAD1 is disabled (enable_vad1=false in run_config)"
        echo "Please set enable_vad1=true in run_config or run a different test"
        exit 1
    fi
fi

# Define common Java runtime parameters
DEBUG_OPTS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer  -XX:NativeMemoryTracking=detail -XX:+PrintCommandLineFlags"
CRASH_OPTS="-XX:ErrorFile=./logs/hs_err_pid%p.log -XX:LogFile=./logs/jvm.log -XX:+CreateMinidumpOnCrash -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/"

# -Xcheck:jni -Xss4m -XX:MaxJNILocalCapacity=4096 -Xms512m -Xmx2048m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m
JAVA_OPTS="$DEBUG_OPTS $CRASH_OPTS $JNI_OPTS"

# Set core dump related configuration
ulimit -c unlimited
# Create core file directory
mkdir -p ./logs
chmod 777 ./logs

# Set system core dump behavior
#cd ./logs
#CORE_PATH=$(pwd)
#cd ..
#echo "core dump path: $CORE_PATH"
# Set core file save path
#echo "$CORE_PATH/core.%e.%p" >/proc/sys/kernel/core_pattern

# Add signal handling related options
export JAVA_OPTS="$JAVA_OPTS -XX:+UseSignalChaining"

# Memory debugging options
# export MALLOC_CHECK_=7
# export MALLOC_PERTURB_=42

# Unset environment variables that may cause issues
unset _JAVA_OPTIONS
unset JAVA_TOOL_OPTIONS

# Set additional system limits
ulimit -s unlimited # Set stack size to unlimited

CLASSPATH=".:third_party/commons-cli-1.5.0.jar:third_party/junit-4.13.2.jar:third_party/log4j-api-2.24.3.jar:third_party/log4j-core-2.24.3.jar:./libs/agora-sdk.jar:./build"

# Ensure Java library path includes all necessary directories
JAVA_LIBRARY_PATH="-Djava.library.path=libs:$LIB_PATH:third_party"

# Check if ASAN is enabled either from config or command line
if [ "$ENABLE_ASAN" = "true" ]; then
    echo "ASAN is enabled"
    # Set ASAN related environment variables
    export LD_PRELOAD=/usr/lib/gcc/x86_64-linux-gnu/13/libasan.so
    export ASAN_OPTIONS=detect_container_overflow=0
fi

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Execute Java command with updated java.library.path
java -cp $CLASSPATH $JAVA_OPTS $JAVA_LIBRARY_PATH -Dlog.filename=app-$TIMESTAMP -Dlog4j.configurationFile=file:./log4j2.xml $CLASS $*
