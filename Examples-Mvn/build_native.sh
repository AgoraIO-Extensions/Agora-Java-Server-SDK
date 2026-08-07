#!/bin/bash
set -x
set -e

# 初始化标志
build_ffmpegUtils=false
build_mediaUtils=false

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case "$1" in
    -ffmpegUtils)
        build_ffmpegUtils=true
        shift
        ;;
    -mediaUtils)
        build_mediaUtils=true
        shift
        ;;
    *)
        echo "Unknown option: $1"
        exit 1
        ;;
    esac
done

# 设置路径
PROJECT_DIR=$(pwd)
NATIVE_DIR="src/main/native"
BUILD_DIR="target/native"
THIRD_PARTY_DIR="third_party"

# 创建构建目录
mkdir -p "$BUILD_DIR"
mkdir -p "$THIRD_PARTY_DIR"

# 检查Java环境
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set"
    exit 1
fi

INCLUDE_ARCH=$(ls -d -1 $JAVA_HOME/include/*/ 2>/dev/null | head -n 1)
if [ -z "$INCLUDE_ARCH" ]; then
    echo "ERROR: Cannot find Java include directory"
    exit 1
fi

# 如果有 -ffmpegUtils 参数，则编译 libffmpeg_utils.so
if $build_ffmpegUtils; then
    echo "Building libffmpeg_utils.so..."

    # 检查FFmpeg依赖
    if ! pkg-config --exists libavcodec libavformat libavutil libswresample libswscale; then
        echo "ERROR: FFmpeg development libraries not found. Please install them first."
        echo "Run: sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev libswresample-dev"
        exit 1
    fi

    FFMPEG_CFLAGS=$(pkg-config --cflags libavcodec libavformat libavutil libswresample libswscale)
    FFMPEG_LIBS=$(pkg-config --libs libavcodec libavformat libavutil libswresample libswscale)

    gcc -shared -o "$BUILD_DIR/libffmpeg_utils.so" -fPIC \
        -I"$JAVA_HOME/include" \
        -I"$JAVA_HOME/include/linux" \
        $FFMPEG_CFLAGS \
        "$NATIVE_DIR/ffmpeg_utils/media_decode_jni.cc" \
        "$NATIVE_DIR/ffmpeg_utils/decode_media.c" \
        $FFMPEG_LIBS

    cp -f "$BUILD_DIR/libffmpeg_utils.so" "$THIRD_PARTY_DIR/"
    echo "libffmpeg_utils.so built successfully"
fi

# 如果有 -mediaUtils 参数，则编译 libmedia_utils.so
if $build_mediaUtils; then
    echo "Building libmedia_utils.so..."

    MEDIA_UTILS_DIR="$NATIVE_DIR/media_utils"
    OPUS_INCLUDE_DIR="$MEDIA_UTILS_DIR/third-party/opusfile_parser/include"
    OPUS_LIB_DIR="$MEDIA_UTILS_DIR/third-party/opusfile_parser/lib"

    # 检查Opus依赖
    if [ ! -d "$OPUS_LIB_DIR" ]; then
        echo "ERROR: Opus library directory not found at $OPUS_LIB_DIR"
        exit 1
    fi

    # 编译各个对象文件
    g++ -c -std=c++11 -fPIC -g \
        -I"$MEDIA_UTILS_DIR" \
        -I"$OPUS_INCLUDE_DIR" \
        -I"$JAVA_HOME/include" \
        -I"$INCLUDE_ARCH" \
        "$MEDIA_UTILS_DIR/helper_aac_parser.cpp" \
        -o "$BUILD_DIR/helper_aac_parser.o"

    g++ -c -std=c++11 -fPIC -g \
        -I"$MEDIA_UTILS_DIR" \
        -I"$OPUS_INCLUDE_DIR" \
        -I"$JAVA_HOME/include" \
        -I"$INCLUDE_ARCH" \
        "$MEDIA_UTILS_DIR/helper_opus_parser.cpp" \
        -o "$BUILD_DIR/helper_opus_parser.o"

    g++ -c -std=c++11 -fPIC -g \
        -I"$MEDIA_UTILS_DIR" \
        -I"$OPUS_INCLUDE_DIR" \
        -I"$JAVA_HOME/include" \
        -I"$INCLUDE_ARCH" \
        "$MEDIA_UTILS_DIR/helper_h264_parser.cpp" \
        -o "$BUILD_DIR/helper_h264_parser.o"

    g++ -c -std=c++11 -fPIC -g \
        -I"$MEDIA_UTILS_DIR" \
        -I"$OPUS_INCLUDE_DIR" \
        -I"$JAVA_HOME/include" \
        -I"$INCLUDE_ARCH" \
        "$MEDIA_UTILS_DIR/helper_h265_parser.cpp" \
        -o "$BUILD_DIR/helper_h265_parser.o"

    g++ -c -std=c++11 -fPIC -g \
        -I"$MEDIA_UTILS_DIR" \
        -I"$OPUS_INCLUDE_DIR" \
        -I"$JAVA_HOME/include" \
        -I"$INCLUDE_ARCH" \
        "$MEDIA_UTILS_DIR/helper_vp8_parser.cpp" \
        -o "$BUILD_DIR/helper_vp8_parser.o"

    g++ -c -std=c++11 -fPIC -g \
        -I"$MEDIA_UTILS_DIR" \
        -I"$OPUS_INCLUDE_DIR" \
        -I"$JAVA_HOME/include" \
        -I"$INCLUDE_ARCH" \
        "$MEDIA_UTILS_DIR/native-lib.cpp" \
        -o "$BUILD_DIR/native-lib.o"

    # 链接生成动态库
    g++ -shared -std=c++11 -fPIC -g \
        "$BUILD_DIR/helper_vp8_parser.o" \
        "$BUILD_DIR/helper_aac_parser.o" \
        "$BUILD_DIR/helper_h264_parser.o" \
        "$BUILD_DIR/helper_h265_parser.o" \
        "$BUILD_DIR/native-lib.o" \
        "$BUILD_DIR/helper_opus_parser.o" \
        -L"$OPUS_LIB_DIR" \
        -lopusfile -logg -lopus \
        -o "$BUILD_DIR/libmedia_utils.so"

    cp -f "$BUILD_DIR/libmedia_utils.so" "$THIRD_PARTY_DIR/"
    echo "libmedia_utils.so built successfully"
fi

# 读取配置文件
CONFIG_FILE="run_config"
ENABLE_GATEWAY=false

if [ -f "$CONFIG_FILE" ]; then
    echo "Reading configuration from $CONFIG_FILE"
    # 读取配置文件中的设置
    while IFS='=' read -r key value; do
        # 忽略注释行和空行
        if [[ "$key" =~ ^[[:space:]]*# ]] || [[ -z "$key" ]]; then
            continue
        fi
        if [ "$key" = "enable_gateway" ]; then
            ENABLE_GATEWAY=$value
        fi
    done <"$CONFIG_FILE"
fi

echo "Build completed successfully"
