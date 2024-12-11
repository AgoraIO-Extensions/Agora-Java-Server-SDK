#!/usr/bin/env zsh
#sh run.zsh io.agora.rtc.test.H264PcmTest -channelId  aga -token 123 -sampleRate 23900

export DYLD_LIBRARY_PATH=$(pwd)/libs/
export LD_LIBRARY_PATH=$(pwd)/libs/
echo "$LD_LIBRARY_PATH"

CLASS=$1
shift 1

echo "Parameters passed to run.zsh: $@"

for arg in "$@"; do
    last_arg="$arg"
done

# 检查最后一个参数是否为 -asan
if [ "$last_arg" = "-asan" ]; then
    export LD_PRELOAD=/usr/lib/gcc/x86_64-linux-gnu/13/libasan.so
    ASAN_OPTIONS=detect_container_overflow=0 java -cp .:third_party/commons-cli-1.5.0.jar:third_party/junit-4.13.2.jar:./libs/agora-sdk.jar:./build -Xcheck:jni -XX:+HeapDumpOnOutOfMemoryError -Djava.library.path=libs $CLASS $* | grep -v "WARNING in native method: JNI call made without checking exceptions"
else
    java -cp .:third_party/commons-cli-1.5.0.jar:third_party/junit-4.13.2.jar:./libs/agora-sdk.jar:./build -Xcheck:jni -XX:+HeapDumpOnOutOfMemoryError -Djava.library.path=libs $CLASS $* | grep -v "WARNING in native method: JNI call made without checking exceptions"
fi
