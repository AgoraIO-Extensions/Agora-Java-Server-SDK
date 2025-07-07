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

OUT=build
INCLUDE_FFMPEG_UTILS=src/cpp/ffmpeg_utils
INCLUDE_MEDIA_UTILS=src/cpp/media_utils
INCLUDE_ARCH=$(ls -d -1 $JAVA_HOME/include/*/)

rm -fr build
mkdir -p build

# 如果有 -ffmpegUtils 参数，则编译 libffmpeg_utils.so
if $build_ffmpegUtils; then
    echo "Building libffmpeg_utils.so..."

    FFMPEG_CFLAGS=$(pkg-config --cflags libavcodec libavformat libavutil libswresample libswscale)
    FFMPEG_LIBS=$(pkg-config --libs libavcodec libavformat libavutil libswresample libswscale)

    gcc -shared -o $OUT/libffmpeg_utils.so -fPIC \
        -I$JAVA_HOME/include \
        -I$JAVA_HOME/include/linux \
        $FFMPEG_CFLAGS \
        $INCLUDE_FFMPEG_UTILS/media_decode_jni.cc \
        $INCLUDE_FFMPEG_UTILS/decode_media.c \
        $FFMPEG_LIBS

    cp -f "$OUT/libffmpeg_utils.so" "third_party/libffmpeg_utils.so"
    cp -f "$OUT/libffmpeg_utils.so" "../Examples-Mvn/third_party/libffmpeg_utils.so"

fi

if $build_mediaUtils; then
    g++ -c -std=c++11 -fPIC -g -I$INCLUDE_MEDIA_UTILS -I$INCLUDE_MEDIA_UTILS/third-party/opusfile_parser/include -I$JAVA_HOME/include -I$INCLUDE_ARCH $INCLUDE_MEDIA_UTILS/helper_aac_parser.cpp -o $OUT/helper_aac_parser.o
    g++ -c -std=c++11 -fPIC -g -I$INCLUDE_MEDIA_UTILS -I$INCLUDE_MEDIA_UTILS/third-party/opusfile_parser/include -I$JAVA_HOME/include -I$INCLUDE_ARCH $INCLUDE_MEDIA_UTILS/helper_opus_parser.cpp -o $OUT/helper_opus_parser.o
    g++ -c -std=c++11 -fPIC -g -I$INCLUDE_MEDIA_UTILS -I$INCLUDE_MEDIA_UTILS/third-party/opusfile_parser/include -I$JAVA_HOME/include -I$INCLUDE_ARCH $INCLUDE_MEDIA_UTILS/helper_h264_parser.cpp -o $OUT/helper_h264_parser.o
    g++ -c -std=c++11 -fPIC -g -I$INCLUDE_MEDIA_UTILS -I$INCLUDE_MEDIA_UTILS/third-party/opusfile_parser/include -I$JAVA_HOME/include -I$INCLUDE_ARCH $INCLUDE_MEDIA_UTILS/helper_vp8_parser.cpp -o $OUT/helper_vp8_parser.o
    g++ -c -std=c++11 -fPIC -g -I$INCLUDE_MEDIA_UTILS -I$INCLUDE_MEDIA_UTILS/third-party/opusfile_parser/include -I$JAVA_HOME/include -I$INCLUDE_ARCH $INCLUDE_MEDIA_UTILS/native-lib.cpp -o $OUT/native-lib.o
    g++ -shared -std=c++11 -fPIC -g $OUT/helper_vp8_parser.o $OUT/helper_aac_parser.o $OUT/helper_h264_parser.o $OUT/native-lib.o $OUT/helper_opus_parser.o -L$INCLUDE_MEDIA_UTILS/third-party/opusfile_parser/lib/ -lopusfile -logg -lopus -o $OUT/libmedia_utils.so
    cp -f "$OUT/libmedia_utils.so" "third_party/libmedia_utils.so"
    cp -f "$OUT/libmedia_utils.so" "../Examples-Mvn/third_party/libmedia_utils.so"
fi

# 读取配置文件
CONFIG_FILE="run_config"
ENABLE_AED_VAD=false
ENABLE_GATEWAY=false

if [ -f "$CONFIG_FILE" ]; then
    echo "Reading configuration from $CONFIG_FILE"
    # 读取配置文件中的设置
    while IFS='=' read -r key value; do
        if [ "$key" = "enable_aed_vad" ]; then
            ENABLE_AED_VAD=$value
        fi
        if [ "$key" = "enable_gateway" ]; then
            ENABLE_GATEWAY=$value
        fi
    done <"$CONFIG_FILE"
fi

# 根据VAD1配置决定是否排除VadV1Test.java
find_java_cmd="find src -name '*.java'"
if [ "$ENABLE_AED_VAD" = "false" ]; then
    echo "AED VAD is disabled, excluding VadV1Test.java from compilation"
    find_java_cmd+=" | grep -v 'VadV1Test.java'"
fi

if [ "$ENABLE_GATEWAY" = "false" ]; then
    echo "Gateway is disabled, excluding Audio3aTest.java from compilation"
    find_java_cmd+=" | grep -v 'Audio3aTest.java'"
fi

# 执行find命令并生成source文件
eval "$find_java_cmd" >build/test_source.txt

# 编译 Java 文件
javac -g -cp .:third_party/log4j-api-2.24.3.jar:third_party/log4j-core-2.24.3.jar:third_party/commons-cli-1.5.0.jar:third_party/junit-4.13.2.jar:libs/agora-sdk.jar -encoding utf-8 @build/test_source.txt -d build -XDignore.symbol.file -Xlint:-options

echo "Build completed successfully"
