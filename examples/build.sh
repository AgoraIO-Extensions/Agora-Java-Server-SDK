#!/bin/bash
set -x
set -e

# 初始化标志
build_ff=false

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case "$1" in
    -ff)
        build_ff=true
        shift
        ;;
    *)
        echo "Unknown option: $1"
        exit 1
        ;;
    esac
done

rm -fr build
mkdir -p build

rm -fr libs/libmedia_decode.so

if $build_ff; then
    echo "Building libmedia_decode.so..."

    FFMPEG_INCLUDE_DIR=/usr/local/include
    FFMPEG_LIB_DIR=/usr/local/lib

    gcc -shared -o libs/libmedia_decode.so -fPIC \
        -I$JAVA_HOME/include \
        -I$JAVA_HOME/include/linux \
        -I$FFMPEG_INCLUDE_DIR \
        -L$FFMPEG_LIB_DIR \
        src/cpp/media_decode_jni.cc \
        src/cpp/decode_media.c \
        -lavformat -lavcodec -lavutil -lswresample -lswscale \
        -Wl,-rpath=$FFMPEG_LIB_DIR
fi

cp -f "third_party/libmediautils.so" "libs/libmediautils.so"

find src -name "*.java" >build/test_source.txt

# 编译 Java 文件
javac -g -cp .:third_party/commons-cli-1.5.0.jar:third_party/junit-4.13.2.jar:libs/agora-sdk.jar -encoding utf-8 @build/test_source.txt -d build -XDignore.symbol.file

echo "Build completed successfully"
