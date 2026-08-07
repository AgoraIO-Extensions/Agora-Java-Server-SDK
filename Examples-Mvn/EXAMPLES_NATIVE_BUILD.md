# Native C++ Code Integration in Maven Project

## 概述

本项目已集成了C++代码编译功能，可以在Maven构建过程中自动编译生成所需的.so库文件。

## 项目结构

```
Examples-Mvn/
├── src/main/native/           # C++源码目录
│   ├── ffmpeg_utils/         # FFmpeg相关的C++代码
│   │   ├── decode_media.c
│   │   ├── decode_media.h
│   │   └── media_decode_jni.cc
│   └── media_utils/          # 媒体处理相关的C++代码
│       ├── helper_*.cpp      # 各种解析器
│       ├── native-lib.cpp    # JNI接口
│       └── third-party/      # 第三方依赖
├── build_native.sh           # C++编译脚本
├── build.sh             # Maven构建脚本
├── third_party/             # 编译输出的.so文件
└── target/native/           # 编译中间文件
```

## 环境要求

### 系统依赖
- Linux 操作系统
- GCC/G++ 编译器
- pkg-config
- Java 开发环境 (JAVA_HOME 必须设置)

### FFmpeg依赖 (如果需要编译FFmpeg相关功能)
```bash
sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev libswresample-dev
```

## 使用方法

### 1. 标准Maven构建 (不编译native代码)
```bash
./build.sh
```

### 2. 编译所有native库
```bash
./build.sh -native
```

### 3. 只编译FFmpeg相关库
```bash
./build.sh -ffmpegUtils
```

### 4. 只编译Media相关库
```bash
./build.sh -mediaUtils
```

### 5. 编译并启动应用
```bash
./build.sh -native start
```

## Maven Profile

项目定义了三个Maven Profile：

1. **build-native**: 编译所有native库
   ```bash
   mvn clean package -Dbuild.native=true
   ```

2. **build-ffmpeg**: 只编译FFmpeg库
   ```bash
   mvn clean package -Dbuild.ffmpeg=true
   ```

3. **build-media**: 只编译Media库
   ```bash
   mvn clean package -Dbuild.media=true
   ```

## 直接使用构建脚本

你也可以直接使用`build_native.sh`脚本：

```bash
# 编译FFmpeg库
./build_native.sh -ffmpegUtils

# 编译Media库
./build_native.sh -mediaUtils

# 编译所有库
./build_native.sh -ffmpegUtils -mediaUtils
```

## 输出文件

编译成功后，.so文件会自动复制到：
- `third_party/libffmpeg_utils.so` - FFmpeg相关库
- `third_party/libmedia_utils.so` - Media相关库

## 配置文件

项目仍然支持`run_config`文件来控制功能开关：

```properties
enable_asan=false
enable_gateway=false
```

## 故障排除

### 常见问题

1. **JAVA_HOME未设置**
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
   ```

2. **FFmpeg依赖缺失**
   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev libswresample-dev
   ```

3. **权限问题**
   ```bash
   chmod +x build_native.sh
   chmod +x build.sh
   ```

4. **Opus库路径问题**
   
   确保`src/main/native/media_utils/third-party/opusfile_parser/lib/`目录存在且包含必要的库文件。

### 查看详细构建日志

构建脚本使用了`set -x`，会输出详细的执行过程。如果遇到问题，可以查看完整的编译命令和错误信息。

## 集成到IDE

如果你使用IDE（如IntelliJ IDEA），可以：

1. 导入Maven项目
2. 在IDE中执行Maven goal: `compile -Dbuild.native=true`
3. 或者在终端中使用构建脚本

## 注意事项

- 首次编译可能需要较长时间，因为需要编译所有C++源码
- 编译过程会在`target/native/`目录生成中间文件
- 如果修改了C++源码，需要重新编译相应的库
- 生成的.so文件会自动包含在应用的classpath中 