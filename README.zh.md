# Agora Linux Server Java SDK

中文 | [English](./README.md)

## 目录

- [Agora Linux Server Java SDK](#agora-linux-server-java-sdk)
  - [目录](#目录)
  - [简介](#简介)
  - [开发环境要求](#开发环境要求)
    - [硬件环境](#硬件环境)
    - [软件环境](#软件环境)
  - [SDK 下载](#sdk-下载)
    - [Maven 下载](#maven-下载)
    - [CDN 下载](#cdn-下载)
  - [集成 SDK](#集成-sdk)
    - [1. Maven 集成](#1-maven-集成)
      - [1.1 添加 Maven 依赖](#11-添加-maven-依赖)
      - [1.2 集成 so 库文件](#12-集成-so-库文件)
    - [2. 本地 SDK 集成](#2-本地-sdk-集成)
      - [2.1 SDK 包结构](#21-sdk-包结构)
      - [2.2 集成 JAR 文件](#22-集成-jar-文件)
          - [本地 Maven 仓库方法](#本地-maven-仓库方法)
          - [直接引用方法](#直接引用方法)
      - [2.3 集成 so 库文件](#23-集成-so-库文件)
    - [3. 加载原生库 (.so 文件)](#3-加载原生库-so-文件)
      - [3.1 提取 so 库文件](#31-提取-so-库文件)
      - [3.2 配置加载路径](#32-配置加载路径)
  - [快速开始](#快速开始)
    - [官方示例文档](#官方示例文档)
    - [开通服务](#开通服务)
    - [运行 Examples-Mvn 示例工程](#运行-examples-mvn-示例工程)
      - [环境准备](#环境准备)
      - [项目配置](#项目配置)
      - [编译构建](#编译构建)
      - [运行示例](#运行示例)
      - [测试 case](#测试-case)
  - [API 参考](#api-参考)
    - [API 文档参考](#api-文档参考)
    - [VAD 模块](#vad-模块)
      - [VadV1 模块（仅支持Gateway SDK）](#vadv1-模块仅支持gateway-sdk)
        - [介绍](#介绍)
        - [类和方法](#类和方法)
          - [AgoraAudioVad 类](#agoraaudiovad-类)
          - [AgoraAudioVadConfig 类](#agoraaudiovadconfig-类)
        - [使用示例](#使用示例)
      - [VadV2 模块](#vadv2-模块)
        - [介绍](#介绍-1)
        - [类和方法](#类和方法-1)
          - [AgoraAudioVadV2 类](#agoraaudiovadv2-类)
          - [AgoraAudioVadConfigV2 属性](#agoraaudiovadconfigv2-属性)
          - [注意事项](#注意事项)
          - [方法](#方法)
        - [VadProcessResult](#vadprocessresult)
          - [构造方法](#构造方法)
        - [使用示例](#使用示例-1)
    - [Audio 3A 模块（仅支持Gateway SDK）](#audio-3a-模块仅支持gateway-sdk)
      - [介绍](#介绍-2)
      - [类和方法](#类和方法-2)
        - [AgoraAudioProcessor 类](#agoraaudioprocessor-类)
          - [构造方法](#构造方法-1)
          - [方法](#方法-1)
        - [AgoraAudioProcessorConfig 类](#agoraaudioprocessorconfig-类)
          - [方法](#方法-2)
          - [示例](#示例)
        - [IAgoraAudioProcessorEventHandler 接口](#iagoraaudioprocessoreventhandler-接口)
          - [方法](#方法-3)
        - [io.agora.rtc.audio3a.AgoraAudioFrame 类](#ioagorartcaudio3aagoraaudioframe-类)
          - [关键属性](#关键属性)
          - [主要方法 (Setters/Getters)](#主要方法-settersgetters)
      - [使用示例](#使用示例-2)
  - [更新日志](#更新日志)
    - [v4.4.32.1（2025-06-12）](#v443212025-06-12)
      - [API 变更](#api-变更)
      - [改进与优化](#改进与优化)
    - [v4.4.32（2025-05-27）](#v44322025-05-27)
      - [API 变更](#api-变更-1)
      - [改进与优化](#改进与优化-1)
    - [v4.4.31.4（2025-03-21）](#v443142025-03-21)
      - [改进与优化](#改进与优化-2)
    - [v4.4.31.3（2025-02-26）](#v443132025-02-26)
      - [改进与优化](#改进与优化-3)
    - [v4.4.31.2（2025-02-19）](#v443122025-02-19)
      - [API 变更](#api-变更-2)
      - [改进与优化](#改进与优化-4)
    - [v4.4.31.1（2025-01-06）](#v443112025-01-06)
      - [改进与优化](#改进与优化-5)
    - [v4.4.31（2024-12-23）](#v44312024-12-23)
      - [API 变更](#api-变更-3)
      - [改进与优化](#改进与优化-6)
    - [v4.4.30.2（2024-11-20）](#v443022024-11-20)
      - [API 变更](#api-变更-4)
      - [改进与优化](#改进与优化-7)
    - [v4.4.30.1（2024-11-12）](#v443012024-11-12)
      - [API 变更](#api-变更-5)
      - [改进与优化](#改进与优化-8)
    - [v4.4.30（2024-10-24）](#v44302024-10-24)
  - [其他参考](#其他参考)
  - [其他参考](#其他参考-1)

## 简介

Agora Linux Server Java SDK (v4.4.32.1) 为您提供了强大的实时音视频通信能力，可无缝集成到 Linux 服务器端 Java 应用程序中。借助此 SDK，您的服务器可以作为数据源或处理节点加入 Agora 频道，实时获取和处理音视频流，从而实现多种业务相关的其他高级功能。

Agora Linux Gateway SDK 暂未发布，相关功能暂未支持。

## 开发环境要求

### 硬件环境

- **操作系统**：Ubuntu 18.04+ 或 CentOS 7.0+
- **CPU 架构**：x86-64
- **性能要求**：
  - CPU：8 核 1.8 GHz 或更高
  - 内存：2 GB（推荐 4 GB+）
- **网络要求**：
  - 公网 IP
  - 允许访问 `.agora.io` 和 `.agoralab.co` 域名

### 软件环境

- Apache Maven 或其他构建工具
- JDK 8+

## SDK 下载

### Maven 下载

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.1</version>
</dependency>
```

### CDN 下载

[Agora-Linux-Java-SDK-v4.4.32.1-x86_64-675656-1c0b814025-20250612_105900](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.32.1-x86_64-675656-1c0b814025-20250612_105900.zip)

## 集成 SDK

SDK 集成有两种方式：通过 Maven 集成和本地 SDK 集成。

### 1. Maven 集成

Maven 集成是最简单的方式，可以自动管理 Java 依赖关系。

#### 1.1 添加 Maven 依赖

在项目的 `pom.xml` 文件中添加以下依赖：

```xml
<!-- x86_64 平台 -->
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.1</version>
</dependency>
```

#### 1.2 集成 so 库文件

Maven 依赖包含了所需的 JAR 文件，但仍需手动处理 `.so` 库文件才能运行。请参考下面的 **加载原生库 (.so 文件)** 部分。

### 2. 本地 SDK 集成

本地 SDK 是一个包含所有必要文件的完整包，适合需要更灵活控制的场景。

#### 2.1 SDK 包结构

从官网下载的 SDK 包（zip 格式）包含以下内容：

- **doc/** - JavaDoc 文档，详细的 API 说明
- **examples/** - 示例代码和项目
- **sdk/** - 核心 SDK 文件
  - `agora-sdk.jar` - Java 类库
  - `agora-sdk-javadoc.jar` - JavaDoc 文档

#### 2.2 集成 JAR 文件

你可以通过两种方式集成 JAR 文件：

###### 本地 Maven 仓库方法

方法一：只安装 SDK JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32.1 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

方法二：同时安装 SDK JAR 和 JavaDoc JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32.1 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -Djavadoc=sdk/agora-sdk-javadoc.jar
```

安装后，在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.1</version>
</dependency>
```

###### 直接引用方法

1. 将 JAR 文件复制到项目的 `libs` 目录：

   ```sh
   mkdir -p libs
   cp sdk/agora-sdk.jar libs/
   cp sdk/agora-sdk-javadoc.jar libs/  # 可选，用于 IDE 支持
   ```

2. 在 Java 项目中添加 classpath 引用：

   ```sh
   # 使用 SDK JAR
   java -cp .:libs/agora-sdk.jar 你的主类

   # 在 IDE 中配置 JavaDoc（常见的 IDE 如 IntelliJ IDEA 或 Eclipse 支持直接关联 JavaDoc JAR）
   ```

#### 2.3 集成 so 库文件

下载的 SDK 包中已经包含了 `.so` 文件。你需要确保 Java 程序运行时能够找到这些文件。请参考下面的 **加载原生库 (.so 文件)** 部分。

### 3. 加载原生库 (.so 文件)

Agora Linux Server Java SDK 依赖于底层的 C++ 原生库（`.so` 文件）。无论是通过 Maven 集成还是本地集成，都需要确保 Java 虚拟机 (JVM) 在运行时能够找到并加载这些库。

#### 3.1 提取 so 库文件

`.so` 文件包含在 `agora-sdk.jar` 或 `linux-java-sdk-x.x.x.x.jar` 文件内部。你需要先将它们提取出来：

1.  在你的项目或部署目录下创建一个用于存放库文件的目录，例如 `libs`：

    ```sh
    mkdir -p libs
    cd libs
    ```

2.  使用 `jar` 命令从 SDK 的 JAR 文件中提取内容（假设 JAR 文件位于 `libs` 目录下或 Maven 缓存中）：

    ```sh
    # 如果使用本地集成方式，JAR 文件通常在 libs 目录下
    jar xvf agora-sdk.jar

    # 如果使用 Maven 集成方式，JAR 文件在 Maven 缓存中，例如：
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-java-sdk/4.4.32.1/linux-java-sdk-4.4.32.1.jar
    ```

3.  提取后，`libs` 目录下会生成 `native/linux/x86_64` 子目录，其中包含所需的 `.so` 文件：

    ```
    libs/
    ├── agora-sdk.jar (或者空的，如果仅用于提取)
    ├── io/          # Java 的 class 类所在，无需关注
    ├── META-INF/    # JAR 文件和应用程序相关的元数据，无需关注
    └── native/      # 对应平台的 so 库文件
        └── linux/
            └── x86_64/   # x86_64 平台 so 库
                ├── libagora_rtc_sdk.so
                ├── libagora-fdkaac.so
                ├── libaosl.so
                └── libbinding.so
    ```

#### 3.2 配置加载路径

有两种主要方法让 JVM 找到 `.so` 文件：

**方法一：通过设置环境变量 `LD_LIBRARY_PATH` (推荐)**

这是最可靠的方式，特别是在 `.so` 文件之间存在依赖关系时。

```sh
# 确定你的 .so 文件所在的目录，假设在 ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# 设置 LD_LIBRARY_PATH 环境变量，将库目录添加到现有路径的前面
export LD_LIBRARY_PATH=$LIB_DIR:$LD_LIBRARY_PATH

# 运行你的 Java 应用
java -jar 你的应用.jar
# 或者使用 classpath
# java -cp "你的classpath" 你的主类
```

**方法二：通过 JVM 参数 `-Djava.library.path`**

这种方法直接告诉 JVM 在哪里查找库文件。

```sh
# 确定你的 .so 文件所在的目录，假设在 ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# 运行 Java 应用，并通过 -D 参数指定库路径
java -Djava.library.path=$LIB_DIR -jar 你的应用.jar
# 或者使用 classpath
# java -Djava.library.path=$LIB_DIR -cp "你的classpath" 你的主类
```

> **注意**：
>
> - 推荐使用方法一 (`LD_LIBRARY_PATH`)，因为它能更好地处理库之间的依赖。如果仅使用 `-Djava.library.path`，有时可能因为库找不到其依赖的其他库而加载失败。
> - 确保 `$LIB_DIR` 指向包含 `libagora_rtc_sdk.so` 等文件的 **确切目录**。
> - 你可以将设置环境变量的命令放入启动脚本中，以便每次运行应用时自动配置。

参考以下脚本示例，它结合了两种方法，并设置了 classpath：

```sh
#!/bin/bash
# 获取当前脚本所在目录的绝对路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 确定 so 库文件路径 (假设在脚本目录下的 libs/native/linux/x86_64)
LIB_PATH="$SCRIPT_DIR/libs/native/linux/x86_64"
# SDK JAR 路径 (假设在脚本目录下的 libs)
SDK_JAR="$SCRIPT_DIR/libs/agora-sdk.jar"
# 你的应用主类
MAIN_CLASS="你的主类"
# 你的应用的其他依赖 classpath (如果有)
APP_CP="你的其他classpath"

# 设置库路径环境变量
export LD_LIBRARY_PATH=$LIB_PATH:$LD_LIBRARY_PATH

# 组合 classpath
CLASSPATH=".:$SDK_JAR:$APP_CP" # '.' 表示当前目录

# 执行 Java 程序
# 同时使用 LD_LIBRARY_PATH 和 -Djava.library.path 以确保兼容性
java -Djava.library.path=$LIB_PATH -cp "$CLASSPATH" $MAIN_CLASS
```

## 快速开始

### 官方示例文档

参考 [官方示例文档](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/run-example)

### 开通服务

参考 [官网开通服务](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/enable-service)


### 运行 Examples-Mvn 示例工程

**Examples-Mvn** 是基于 Spring Boot 框架构建的 Maven 示例工程，提供了完整的 RESTful API 服务来演示 Agora Linux Server Java SDK 的各种功能特性。

该工程已集成了C++代码编译功能，可以在Maven构建过程中自动编译生成所需的.so库文件。

#### 环境准备

1. **安装 Maven 构建工具**

   参考 [Maven 安装指南](https://maven.apache.org/install.html)

2. **C++ 编译环境 (如果需要编译native库)**

   安装基本编译工具：
   ```bash
   sudo apt-get update
   sudo apt-get install build-essential pkg-config
   ```

3. **FFmpeg 依赖 (如果需要编译FFmpeg相关功能)**

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev libswresample-dev
   ```

4. **确保 JAVA_HOME 环境变量设置正确**

   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
   ```

#### 项目配置

1. 进入 `Examples-Mvn` 目录：

   ```bash
   cd Examples-Mvn
   ```

2. 创建 `.keys` 文件，添加：

   ```
   APP_ID=your_app_id
   TOKEN=your_token
   ```

   _如果未开启证书，TOKEN 值可为空，例如：_

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

   如果支持Gateway SDK，则需要创建 `.keys_gateway` 文件，添加：

   ```
   APP_ID=your_app_id
   LICENSE=your_license
   ```

3. **运行时配置 (run_config)**

   `run_config` 文件用于配置运行时的各种选项，位于 `Examples-Mvn/run_config`。您可以根据需要修改以下配置：

   | 配置项           | 类型    | 默认值 | 描述                                                              |
   | ---------------- | ------- | ------ | ----------------------------------------------------------------- |
   | enable_jni_check | boolean | false  | 是否启用 JNI 检查，用于调试 JNI 相关问题                          |
   | enable_asan      | boolean | false  | 是否启用 AddressSanitizer，用于内存错误检测                       |
   | enable_aed_vad   | boolean | false  | 是否启用 AED VAD (Audio Event Detection Voice Activity Detection) |
   | enable_gateway   | boolean | false  | 是否启用 Gateway SDK 模式，启用后可使用 VAD 和 Audio 3A 等功能    |

   **配置示例：**

   ```bash
   # 启用 Gateway SDK 功能
   enable_gateway=true
   
   # 启用 JNI 检查（调试模式）
   enable_jni_check=true
   
   # 启用内存检查（调试模式）
   enable_asan=true
   
   # 启用 AED VAD 功能
   enable_aed_vad=true
   ```

   > **注意**：
   > - 启用 `enable_gateway=true` 后，可以使用 VAD 和 Audio 3A 等高级功能
   > - 启用 `enable_jni_check=true` 或 `enable_asan=true` 会影响性能，仅建议在调试时使用
   > - 修改配置后需要重新编译项目

4. **更新Linux Java SDK版本号**

   确保 `pom.xml` 文件中的Linux Java SDK版本号与实际使用的版本一致：

   ```xml
   <dependency>
       <groupId>io.agora.rtc</groupId>
       <artifactId>linux-java-sdk</artifactId>
       <version>4.4.32.1</version>  <!-- 确保版本号正确 -->
   </dependency>
   ```

   > **注意**：版本号不匹配可能导致编译错误或运行时问题，请确保使用与您下载的SDK包对应的正确版本号。

#### 编译构建

执行编译脚本：

```bash
# 标准Maven构建 (不编译native代码)
./build.sh

# 编译并启动服务
./build.sh start

# 编译所有native库
./build.sh -native

# 编译所有native库并启动服务
./build.sh -native start

# 只编译FFmpeg相关库
./build.sh -ffmpegUtils

# 只编译Media相关库  
./build.sh -mediaUtils
```

**编译选项说明：**
- 默认情况下仅编译Java项目，不编译C++代码
- 使用 `-native` 选项编译所有native库（FFmpeg + Media）
- 使用 `-ffmpegUtils` 选项只编译FFmpeg相关库（用于MP4处理）
- 使用 `-mediaUtils` 选项只编译Media相关库（用于编码音视频处理）
- 使用 `start` 选项可在编译完成后自动启动服务

**使用Maven命令：**

你也可以直接使用Maven命令：

```bash
# 编译所有native库
mvn clean package -Dbuild.native=true

# 只编译FFmpeg库
mvn clean package -Dbuild.ffmpeg=true

# 只编译Media库
mvn clean package -Dbuild.media=true
```

#### 运行示例

启动服务后，使用浏览器或 Postman 访问以下接口地址，测试各种功能：

**基础功能测试接口：**
```
http://localhost:18080/api/server/basic?taskName=ReceiverPcmDirectSendTest
http://localhost:18080/api/server/basic?taskName=ReceiverPcmH264Test
http://localhost:18080/api/server/basic?taskName=ReceiverPcmVadTest
http://localhost:18080/api/server/basic?taskName=ReceiverPcmYuvTest
http://localhost:18080/api/server/basic?taskName=SendH264Test
http://localhost:18080/api/server/basic?taskName=SendMp4Test
http://localhost:18080/api/server/basic?taskName=SendOpusTest
http://localhost:18080/api/server/basic?taskName=SendPcmFileTest
http://localhost:18080/api/server/basic?taskName=SendPcmRealTimeTest
http://localhost:18080/api/server/basic?taskName=SendReceiverStreamMessageTest
http://localhost:18080/api/server/basic?taskName=SendYuvTest
```

**Gateway SDK 专属功能：**
```
http://localhost:18080/api/server/basic?taskName=VadV1Test
http://localhost:18080/api/server/basic?taskName=Audio3aTest
```

**配置文件接口：**
```
http://localhost:18080/api/server/start?configFileName=pcm_send.json
```

> **注意**：请将 `localhost:18080` 替换为您的实际服务器地址和端口。


#### 测试 case

- 发送 PCM 音频

  参考 [SendPcmFileTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendPcmFileTest.java),实现循环发送 pcm 文件

  参考 [SendPcmRealTimeTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendPcmRealTimeTest.java),实现发送流式 pcm 数据

- 发送 YUV 视频

  参考 [SendYuvTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendYuvTest.java),实现流式发送 yuv 数据

- 发送 H264 视频

  参考 [SendH264Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendH264Test.java),实现流式发送 h264 数据

- 发送 Opus 音频

  参考 [SendOpusTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendOpusTest.java),实现流式发送 opus 数据

- 发送 MP4 音视频

  参考 [SendMp4Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendMp4Test.java),实现发送 MP4 文件

- 接收 PCM 音频

  参考 [ReceiverPcmVadTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmVadTest.java),实现接收 pcm 数据并携带 VAD 数据

  参考 [ReceiverPcmDirectSendTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmDirectSendTest.java),实现接收 pcm 数据并直接返回发送

- 接收 PCM&H264 音视频

  参考 [ReceiverPcmH264Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmH264Test.java),实现接收 pcm&h264 数据

- 接收 PCM&YUV 音视频

  参考 [ReceiverPcmYuvTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmYuvTest.java),实现接收 pcm&yuv 数据

- 发送接收流消息

  参考 [SendReceiverStreamMessageTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendReceiverStreamMessageTest.java),实现发送接收流消息

- VadV1 模块（仅支持Gateway SDK）

  参考 [VadV1Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/VadV1Test.java.disabled),实现 VadV1 模块

- 音频 3A 处理（仅支持Gateway SDK）

  参考 [Audio3aTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/Audio3aTest.java.disabled),实现音频 3A 处理

## API 参考

### API 文档参考

完整 API 文档可参考以下资源：

- [API-reference.zh.md](./API-reference.zh.md) 文件（仅供参考）
- 官方文档 [Agora Java Server SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)（以官方文档为准）

### VAD 模块

#### VadV1 模块（仅支持Gateway SDK）

##### 介绍

`AgoraAudioVad` 是一个用于处理音频帧的语音活动检测 (VAD) 模块。它可以检测音频流中的语音活动，并根据配置参数进行处理。该模块是 VAD 的第一个版本，提供基础的语音活动检测功能。

##### 类和方法

###### AgoraAudioVad 类

**构造方法**

```java
public AgoraAudioVad()
```

- **描述**：构造一个 `AgoraAudioVad` 实例。

**方法**

```java
public int initialize(AgoraAudioVadConfig config)
```

- **描述**：初始化 VAD 模块。必须在使用其他方法前调用。
- **参数**：
  - `config`：`AgoraAudioVadConfig` 类型，VAD 配置。
- **返回**：`int` 类型，0 表示成功，-1 表示失败。

```java
public VadProcessResult processPcmFrame(byte[] frame)
```

- **描述**：处理 PCM 音频帧。
- **参数**：
  - `frame`：`byte[]` 类型，PCM 音频数据。
- **返回**：`VadProcessResult` 类型，VAD 处理结果。

```java
public synchronized void destroy()
```

- **描述**：销毁 VAD 模块，释放资源。

###### AgoraAudioVadConfig 类

**主要属性**

| 属性名                 | 类型  | 描述                                 | 默认值 | 取值范围               |
| ---------------------- | ----- | ------------------------------------ | ------ | ---------------------- |
| fftSz                  | int   | FFT 大小，仅支持 128、256、512、1024 | 1024   | [128, 256, 512, 1024]  |
| hopSz                  | int   | FFT 跳跃大小，用于检查               | 160    | [1, Integer.MAX_VALUE] |
| anaWindowSz            | int   | FFT 窗口大小，用于计算 RMS           | 768    | [1, Integer.MAX_VALUE] |
| voiceProbThr           | float | 语音概率阈值                         | 0.8    | [0.0, 1.0]             |
| rmsThr                 | float | RMS 阈值（dB）                       | -40.0  | [-100.0, 0.0]          |
| jointThr               | float | 联合阈值（dB）                       | 0.0    | [-100.0, 100.0]        |
| aggressive             | float | 激进因子，值越大越激进               | 5.0    | [0.0, 10.0]            |
| startRecognizeCount    | int   | 开始识别计数                         | 10     | [1, Integer.MAX_VALUE] |
| stopRecognizeCount     | int   | 停止识别计数                         | 6      | [1, Integer.MAX_VALUE] |
| preStartRecognizeCount | int   | 预开始识别计数                       | 10     | [0, Integer.MAX_VALUE] |
| activePercent          | float | 活跃百分比                           | 0.6    | [0.0, 1.0]             |
| inactivePercent        | float | 非活跃百分比                         | 0.2    | [0.0, 1.0]             |

##### 使用示例

下面是一个简单的示例代码，展示如何使用 `AgoraAudioVad` 进行音频帧处理：

```java
import io.agora.rtc.AgoraAudioVad;
import io.agora.rtc.AgoraAudioVadConfig;
import io.agora.rtc.VadProcessResult;
import java.io.FileInputStream;

public class VadV1Example {
    public static void main(String[] args) {
        // 创建 VAD 实例
        AgoraAudioVad audioVad = new AgoraAudioVad();
        
        // 创建配置
        AgoraAudioVadConfig config = new AgoraAudioVadConfig();
        // 可以根据需要调整配置参数，建议使用默认值
        
        // 初始化 VAD
        int ret = audioVad.initialize(config);
        if (ret != 0) {
            System.err.println("Failed to initialize VAD: " + ret);
            return;
        }
        
        // 处理音频帧
        try {
            // 假设有 PCM 音频数据
            byte[] pcmData = new byte[320]; // 10ms 16kHz 单声道 PCM16 数据
            
            VadProcessResult result = audioVad.processPcmFrame(pcmData);
            if (result != null) {
                System.out.println("VAD State: " + result.getState());
                if (result.getOutFrame() != null) {
                    System.out.println("Output Frame Length: " + result.getOutFrame().length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 销毁 VAD 实例
        audioVad.destroy();
    }
}
```

#### VadV2 模块

##### 介绍

`AgoraAudioVadV2` 是一个用于处理音频帧的语音活动检测 (VAD) 模块的第二个版本。它可以检测音频流中的语音活动，并根据配置参数进行处理。

##### 类和方法

###### AgoraAudioVadV2 类

**构造方法**

```java
public AgoraAudioVadV2(AgoraAudioVadConfigV2 config)
```

- **参数**
  - `config`：`AgoraAudioVadConfigV2` 类型，VAD 配置。

###### AgoraAudioVadConfigV2 属性

| 属性名                 | 类型  | 描述                                       | 默认值 | 取值范围               |
| ---------------------- | ----- | ------------------------------------------ | ------ | ---------------------- |
| preStartRecognizeCount | int   | 开始说话状态前保存的音频帧数               | 16     | [0, Integer.MAX_VALUE] |
| startRecognizeCount    | int   | 说话状态的音频帧数                         | 30     | [1, Integer.MAX_VALUE] |
| stopRecognizeCount     | int   | 停止说话状态的音频帧数                     | 20     | [1, Integer.MAX_VALUE] |
| activePercent          | float | 在 startRecognizeCount 帧中活跃帧的百分比  | 0.7    | [0.0, 1.0]             |
| inactivePercent        | float | 在 stopRecognizeCount 帧中非活跃帧的百分比 | 0.5    | [0.0, 1.0]             |
| startVoiceProb         | int   | 开始语音检测的概率阈值                     | 70     | [0, 100]               |
| stopVoiceProb          | int   | 停止语音检测的概率阈值                     | 70     | [0, 100]               |
| startRmsThreshold      | int   | 开始语音检测的 RMS 阈值                    | -50    | [-100, 0]              |
| stopRmsThreshold       | int   | 停止语音检测的 RMS 阈值                    | -50    | [-100, 0]              |

###### 注意事项

- `startVoiceProb`: 值越低，帧被判断为活跃的概率越高，开始阶段会更早开始。在需要更敏感的语音检测时可以适当降低。
- `stopVoiceProb`: 值越高，帧被判断为非活跃的概率越高，结束阶段会更早开始。在需要更快结束语音检测时可以适当提高。
- `startRmsThreshold` 和 `stopRmsThreshold`:
  - 值越高，对语音活动越敏感。
  - 在安静环境中推荐使用默认值 -50。
  - 在嘈杂环境中可以调高到 -40 到 -30 之间，以减少误检。
  - 根据实际使用场景和音频特征进行微调可获得最佳效果。

###### 方法

```java
public synchronized VadProcessResult processFrame(AudioFrame frame)
```

- **参数**
  - `frame`：`AudioFrame` 类型，音频帧。
- **返回**
  - `VadProcessResult` 类型，VAD 处理结果。

```java
public synchronized void destroy()
```

- 销毁 VAD 模块，释放资源。

##### VadProcessResult

存储 VAD 处理结果。

###### 构造方法

```java
public VadProcessResult(byte[] result, Constants.VadState state)
```

- **参数**
  - `result`：`byte[]` 类型，处理后的音频数据。
  - `state`：`Constants.VadState` 类型，当前 VAD 状态。

##### 使用示例

下面是一个简单的示例代码，展示如何使用 `AgoraAudioVadV2` 进行音频帧处理：

```java
import io.agora.rtc.AgoraAudioVadV2;
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.Constants;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.VadProcessResult;

public class VadV2Example {
    public static void main(String[] args) {
        // 创建 VAD 配置
        AgoraAudioVadConfigV2 config = new AgoraAudioVadConfigV2();
        config.setPreStartRecognizeCount(16);
        config.setStartRecognizeCount(30);
        config.setStopRecognizeCount(20);
        config.setActivePercent(0.7f);
        config.setInactivePercent(0.5f);
        config.setStartVoiceProb(70);
        config.setStopVoiceProb(70);
        config.setStartRmsThreshold(-50);
        config.setStopRmsThreshold(-50);

        // 创建 VAD 实例
        AgoraAudioVadV2 vad = new AgoraAudioVadV2(config);

        // 模拟音频帧处理
        AudioFrame frame = new AudioFrame();
        // 设置 frame 的属性...

        VadProcessResult result = vad.processFrame(frame);
        if (result != null) {
            System.out.println("VAD State: " + result.getState());
            System.out.println("Processed Data Length: " + result.getResult().length);
        }

        // 销毁 VAD 实例
        vad.destroy();
    }
}
```

### Audio 3A 模块（仅支持Gateway SDK）

#### 介绍

`AgoraAudioProcessor` 是一个用于音频 3A（AEC、ANS、AGC）以及背景人声抑制（BGHVS）处理的模块。它可以对音频帧进行声学回声消除 (AEC)、自动噪声抑制 (ANS)、自动增益控制 (AGC) 和背景人声抑制 (BGHVS)，以提升音频质量。该模块需要相应的模型文件来执行处理。

#### 类和方法

##### AgoraAudioProcessor 类

###### 构造方法

```java
public AgoraAudioProcessor()
```

- **描述**：构造一个 `AgoraAudioProcessor` 实例。

###### 方法

```java
public int init(String appId, String license, IAgoraAudioProcessorEventHandler eventHandler, AgoraAudioProcessorConfig config)
```

- **描述**：初始化音频处理器。必须在使用其他方法前调用。
- **参数**：
  - `appId`：`String` 类型，声网后台获取的 App ID。
  - `license`：`String` 类型，声网后台获取的 License。
  - `eventHandler`：`IAgoraAudioProcessorEventHandler` 类型，用于接收处理器事件和错误的回调处理器。
  - `config`：`AgoraAudioProcessorConfig` 类型，3A 处理器配置对象，用于配置模型路径等。
- **返回**：`int` 类型，0 表示成功，其他值表示失败。

```java
public AgoraAudioFrame process(AgoraAudioFrame nearIn)
```

- **描述**：对输入的近端音频帧进行 3A 处理（如 ANS、AGC）。当仅处理近端音频，或不需要 AEC 处理时使用此方法。
- **参数**：
  - `nearIn`：`io.agora.rtc.audio3a.AgoraAudioFrame` 类型，包含待处理的近端 PCM 音频数据的帧对象。
- **返回**：`io.agora.rtc.audio3a.AgoraAudioFrame` 类型，处理后的音频帧。如果处理失败，可能返回 `null`。

```java
public AgoraAudioFrame process(AgoraAudioFrame nearIn, AgoraAudioFrame farIn)
```

- **描述**：对输入的近端和远端音频帧进行 3A 处理（如 AEC、ANS、AGC）。当需要进行回声消除 (AEC) 等同时处理近端和远端音频的场景时使用此方法。
- **参数**：
  - `nearIn`：`io.agora.rtc.audio3a.AgoraAudioFrame` 类型，包含待处理的近端 PCM 音频数据的帧对象。
  - `farIn`：`io.agora.rtc.audio3a.AgoraAudioFrame` 类型，包含参考的远端 PCM 音频数据的帧对象，主要用于声学回声消除 (AEC)。
- **返回**：`io.agora.rtc.audio3a.AgoraAudioFrame` 类型，处理后的近端音频帧。如果处理失败，可能返回 `null`。

```java
public int release()
```

- **描述**：释放 `AgoraAudioProcessor` 实例所占用的所有资源。处理完成后应调用此方法。
- **返回**：`int` 类型，0 表示成功，其他值表示失败。

##### AgoraAudioProcessorConfig 类

此类用于配置 `AgoraAudioProcessor`。

###### 方法

```java
public void setModelPath(String modelPath)
```

- **描述**: 设置 3A 处理所需的模型文件路径。模型文件通常随 SDK 包提供，位于 `resources/model/` 目录下。
- **参数**:
  - `modelPath`: `String` 类型，模型文件所在的目录路径。例如 `./resources/model/`。

```java
public void setAecConfig(AecConfig aecConfig)
public AecConfig getAecConfig()
```

- **描述**: 设置和获取声学回声消除（AEC）配置。
- **参数**:
  - `aecConfig`: `AecConfig` 类型，AEC 配置对象。

```java
public void setAnsConfig(AnsConfig ansConfig)
public AnsConfig getAnsConfig()
```

- **描述**: 设置和获取自动噪声抑制（ANS）配置。
- **参数**:
  - `ansConfig`: `AnsConfig` 类型，ANS 配置对象。

```java
public void setAgcConfig(AgcConfig agcConfig)
public AgcConfig getAgcConfig()
```

- **描述**: 设置和获取自动增益控制（AGC）配置。
- **参数**:
  - `agcConfig`: `AgcConfig` 类型，AGC 配置对象。

```java
public void setBghvsConfig(BghvsConfig bghvsConfig)
public BghvsConfig getBghvsConfig()
```

- **描述**: 设置和获取背景人声抑制（BGHVS）配置。
- **参数**:
  - `bghvsConfig`: `BghvsConfig` 类型，BGHVS 配置对象。

###### 示例

```java
AgoraAudioProcessorConfig config = new AgoraAudioProcessorConfig();
config.setModelPath("./resources/model/"); // 根据实际模型文件位置进行设置

// 配置 AEC
AecConfig aecConfig = new AecConfig();
aecConfig.setEnabled(true);
config.setAecConfig(aecConfig);

// 配置 ANS
AnsConfig ansConfig = new AnsConfig();
ansConfig.setEnabled(true);
config.setAnsConfig(ansConfig);

// 配置 AGC
AgcConfig agcConfig = new AgcConfig();
agcConfig.setEnabled(true);
config.setAgcConfig(agcConfig);

// 配置 BGHVS
BghvsConfig bghvsConfig = new BghvsConfig();
bghvsConfig.setEnabled(true);
config.setBghvsConfig(bghvsConfig);
```

##### IAgoraAudioProcessorEventHandler 接口

此接口用于接收来自 `AgoraAudioProcessor` 的事件和错误通知。

###### 方法

```java
public void onEvent(Constants.AgoraAudioProcessorEventType eventType)
```

- **描述**：报告处理器在运行过程中发生的事件。
- **参数**：
  - `eventType`：`io.agora.rtc.Constants.AgoraAudioProcessorEventType` 类型，具体的事件类型。

```java
public void onError(int errorCode)
```

- **描述**：报告处理器在运行过程中发生的错误。
- **参数**：
  - `errorCode`：`int` 类型，错误码，指示发生的具体错误。

##### io.agora.rtc.audio3a.AgoraAudioFrame 类

此类用于封装音频数据以供 `AgoraAudioProcessor` 处理。 (注意：这与 `io.agora.rtc.AudioFrame` 可能不同，请使用 `audio3a` 包下的版本)

###### 关键属性

| 属性名            | 类型       | 描述                                                                                                        |
| ----------------- | ---------- | ----------------------------------------------------------------------------------------------------------- |
| type              | int        | 音频帧类型，通常为 `Constants.AudioFrameType.PCM16.getValue()`。                                            |
| sampleRate        | int        | 音频采样率 (Hz)，例如 16000, 32000, 48000。                                                                 |
| channels          | int        | 音频通道数，例如 1 (单声道) 或 2 (立体声)。                                                                 |
| samplesPerChannel | int        | 每个通道的采样点数量。对于 10ms 的帧，通常是 `sampleRate / 100`。                                           |
| bytesPerSample    | int        | 每个采样点的字节数。例如 PCM16 格式为 2 字节 (`Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue()`)。 |
| buffer            | ByteBuffer | 包含原始 PCM 音频数据的 `java.nio.ByteBuffer`。                                                             |

###### 主要方法 (Setters/Getters)

```java
public void setType(int type);
public int getType();

public void setSampleRate(int sampleRate);
public int getSampleRate();

public void setChannels(int channels);
public int getChannels();

public void setSamplesPerChannel(int samplesPerChannel);
public int getSamplesPerChannel();

public void setBytesPerSample(int bytesPerSample);
public int getBytesPerSample();

public void setBuffer(java.nio.ByteBuffer buffer);
public java.nio.ByteBuffer getBuffer();
```

#### 使用示例

以下是一个简单的示例代码，展示如何使用 `AgoraAudioProcessor` 进行音频帧处理：

```java
import io.agora.rtc.audio3a.AgoraAudioProcessor;
import io.agora.rtc.audio3a.AgoraAudioProcessorConfig;
import io.agora.rtc.audio3a.IAgoraAudioProcessorEventHandler;
import io.agora.rtc.audio3a.AgoraAudioFrame; // 使用 audio3a 包下的 AgoraAudioFrame
import io.agora.rtc.audio3a.AecConfig;
import io.agora.rtc.audio3a.AnsConfig;
import io.agora.rtc.audio3a.AgcConfig;
import io.agora.rtc.audio3a.BghvsConfig;
import io.agora.rtc.Constants; // SDK 的常量类
import java.nio.ByteBuffer;
import java.util.Arrays; // 用于打印数据示例

public class Audio3AProcessingExample {
    public static void main(String[] args) {
        // 替换为您的 App ID 和 License
        String appId = "YOUR_APP_ID";
        String license = "YOUR_LICENSE_KEY";

        // 1. 创建 AgoraAudioProcessor 实例
        AgoraAudioProcessor audioProcessor = new AgoraAudioProcessor();

        // 2. 配置 AgoraAudioProcessorConfig
        AgoraAudioProcessorConfig config = new AgoraAudioProcessorConfig();
        // 设置模型文件路径，通常在 SDK 包的 resources/model/ 目录下
        // 请确保路径正确，否则初始化可能失败
        config.setModelPath("./resources/model/"); // 根据您的实际路径修改

        // 配置 AEC（声学回声消除）
        AecConfig aecConfig = config.getAecConfig();
        aecConfig.setEnabled(true); // 启用 AEC
        
        // 配置 ANS（自动噪声抑制）
        AnsConfig ansConfig = config.getAnsConfig();
        ansConfig.setEnabled(true); // 启用 ANS
        
        // 配置 AGC（自动增益控制）
        AgcConfig agcConfig = config.getAgcConfig();
        agcConfig.setEnabled(true); // 启用 AGC
        
        // 配置 BGHVS（背景人声抑制）
        BghvsConfig bghvsConfig = config.getBghvsConfig();
        bghvsConfig.setEnabled(true); // 启用 BGHVS


        // 3. 初始化 AgoraAudioProcessor
        int initRet = audioProcessor.init(appId, license,
                new IAgoraAudioProcessorEventHandler() {
                    @Override
                    public void onEvent(Constants.AgoraAudioProcessorEventType eventType) {
                        System.out.println("AgoraAudioProcessor Event: " + eventType);
                    }

                    @Override
                    public void onError(int errorCode) {
                        System.err.println("AgoraAudioProcessor Error: " + errorCode);
                    }
                }, config);

        if (initRet != 0) {
            System.err.println("Failed to initialize AgoraAudioProcessor. Error code: " + initRet);
            // 根据错误码处理初始化失败的情况，例如检查 appId, license, modelPath 是否正确
            return;
        }
        System.out.println("AgoraAudioProcessor initialized successfully.");

        // 4. 准备音频帧 (AgoraAudioFrame)
        // 示例参数：48kHz, 单声道, 10ms 音频帧
        int sampleRate = 48000;
        int channels = 1;
        int samplesPerChannel = sampleRate / 100; // 10ms frame -> 480 samples
        int bytesPerSample = Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue(); // PCM16
        int bufferSize = samplesPerChannel * channels * bytesPerSample;

        // 创建近端音频帧
        AgoraAudioFrame nearInFrame = new AgoraAudioFrame();
        nearInFrame.setType(Constants.AudioFrameType.PCM16.getValue());
        nearInFrame.setSampleRate(sampleRate);
        nearInFrame.setChannels(channels);
        nearInFrame.setSamplesPerChannel(samplesPerChannel);
        nearInFrame.setBytesPerSample(bytesPerSample);
        // 实际应用中，这里的 pcmDataNear 来自近端音频源
        byte[] pcmDataNear = new byte[bufferSize]; 
        // ... 此处用虚拟数据填充 pcmDataNear ...
        ByteBuffer nearAudioBuffer = ByteBuffer.allocateDirect(bufferSize);
        nearAudioBuffer.put(pcmDataNear);
        nearAudioBuffer.flip();
        nearInFrame.setBuffer(nearAudioBuffer);

        // 创建远端音频帧 (用于 AEC)
        AgoraAudioFrame farInFrame = new AgoraAudioFrame();
        farInFrame.setType(Constants.AudioFrameType.PCM16.getValue());
        farInFrame.setSampleRate(sampleRate);
        farInFrame.setChannels(channels);
        farInFrame.setSamplesPerChannel(samplesPerChannel);
        farInFrame.setBytesPerSample(bytesPerSample);
        // 实际应用中，这里的 pcmDataFar 来自远端音频源
        byte[] pcmDataFar = new byte[bufferSize]; 
        // ... 此处用虚拟数据填充 pcmDataFar ...
        ByteBuffer farAudioBuffer = ByteBuffer.allocateDirect(bufferSize);
        farAudioBuffer.put(pcmDataFar);
        farAudioBuffer.flip();
        farInFrame.setBuffer(farAudioBuffer);

        // 5. 处理音频帧
        // 如果只需要处理近端音频（例如仅 ANS, AGC），可以调用单参数的 process 方法:
        // AgoraAudioFrame outputFrame = audioProcessor.process(nearInFrame);
 
        // 如果需要 AEC 处理，同时传入近端和远端音频帧
        AgoraAudioFrame outputFrame = audioProcessor.process(nearInFrame, farInFrame);

        if (outputFrame != null && outputFrame.getBuffer() != null) {
            System.out.println("Audio frame processed successfully.");
            ByteBuffer processedBuffer = outputFrame.getBuffer();
            // processedBuffer 包含了经过 3A + BGHVS 处理的音频数据
            // 处理后的音频将具有以下优化：
            // - AEC: 消除声学回声
            // - ANS: 抑制背景噪声
            // - AGC: 自动调节音量增益
            // - BGHVS: 抑制背景人声干扰
            // 您可以将数据写入文件、发送到网络或进行其他操作
            // 例如，获取处理后的字节数据：
            // byte[] processedBytes = new byte[processedBuffer.remaining()];
            // processedBuffer.get(processedBytes);
            // System.out.println("Processed data sample (first 10 bytes): " +
            // Arrays.toString(Arrays.copyOfRange(processedBytes, 0, Math.min(10, processedBytes.length))));
        } else {
            System.err.println("Failed to process audio frame or output frame is null.");
            // 检查是否有错误回调，或 process 方法的返回值
        }

        // 6. 释放资源
        int releaseRet = audioProcessor.release();
        if (releaseRet == 0) {
            System.out.println("AgoraAudioProcessor released successfully.");
        } else {
            System.err.println("Failed to release AgoraAudioProcessor. Error code: " + releaseRet);
        }
    }
}
```

## 更新日志

### v4.4.32.1（2025-06-12）

#### API 变更

- 优化 `ILocalUserObserver` 接口的 `onStreamMessage` 回调参数，将原有的 `onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data, long length)` 修改为 `onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, byte[] data)`，提升消息处理的灵活性和效率。

#### 改进与优化

- 修复了 `AgoraServiceConfig` 的 `setLogFileSize` 方法，单位为 KB 时实际生效单位为 Byte 的问题，现已正确按 KB 设置日志文件大小。

### v4.4.32（2025-05-27）

#### API 变更

- `AgoraService` 新增 `getSdkVersion` 方法，用于获取 SDK 版本号
- `AgoraAudioEncodedFrameSender` 移除 `send(byte[] payloadData, int payloadSize, EncodedAudioFrameInfo info)` 方法，新增 `sendEncodedAudioFrame(byte[] payloadData, EncodedAudioFrameInfo info)` 方法替代
- `AgoraAudioPcmDataSender` 的 `send(byte[] audioData, int captureTimestamp, int samplesPerChannel, int bytesPerSample,
int numberOfChannels, int sampleRate) ` 方法标位不推荐，新增 `sendAudioPcmData(AudioFrame audioFrame)` 方法替代
- `AgoraVideoEncodedImageSender` 移除 `send(byte[] imageBuffer, int length, EncodedVideoFrameInfo info` 方法，新增 `sendEncodedVideoImage(byte[] imageBuffer, EncodedVideoFrameInfo info)` 方法替代
- `AgoraVideoFrameSender` 移除 `send(ExternalVideoFrame frame)` 方法，新增 `sendVideoFrame(ExternalVideoFrame frame)` 方法替代

#### 改进与优化

- 修复了 `destroy` 方法可能导致的崩溃问题

### v4.4.31.4（2025-03-21）

#### 改进与优化

- 修复了多线程环境下可能导致的异常崩溃问题
- 改进了错误处理流程，增强了异常情况下的恢复能力

### v4.4.31.3（2025-02-26）

#### 改进与优化

- 修复了由于内存复用可能导致的异常处理问题

### v4.4.31.2（2025-02-19）

#### API 变更

- 新增 `sendStreamMessage(int streamId, byte[] messageData)` 方法，弃用 `sendStreamMessage(int streamId, String message, int length)` 方法

#### 改进与优化

- 优化代码处理，提高系统稳健性

### v4.4.31.1（2025-01-06）

#### 改进与优化

- 优化 VAD 功能配置，现在默认开启 VAD 功能，无需手动配置

### v4.4.31（2024-12-23）

#### API 变更

- 在 `AgoraServiceConfig` 中新增 `DomainLimit` 配置选项，用于域名限制管理
- 新增 `VadDumpUtils` 工具类，支持导出 VAD 处理过程的调试数据
- 新增 `AudioConsumerUtils` 类，提供优化的 PCM 数据传输机制
- 在 `AgoraLocalUser` 中修改 `registerAudioFrameObserver` 方法，支持配置 `AgoraAudioVadConfigV2` 参数
- 在 `IAudioFrameObserver` 中新增 `onPlaybackAudioFrameBeforeMixing` 回调的 `vadResult` 参数
- 在 `AgoraLocalUser` 类中新增 `sendAudioMetaData` 方法，支持发送音频元数据
- 在 `ILocalUserObserver` 类中新增 `onAudioMetaDataReceived` 回调，用于接收音频元数据
- 在 `ExternalVideoFrame` 类中增加 `ColorSpace` 属性，支持自定义颜色空间设置

#### 改进与优化

- 优化代码逻辑架构，显著提升内存使用效率
- 修复多处内存泄露问题，提高系统稳定性
- 增强内存访问安全机制，有效防止内存踩踏问题

### v4.4.30.2（2024-11-20）

#### API 变更

- 增强了 AgoraAudioVadV2 的 `processFrame` 处理，新增 `START_SPEAKING` 和 `STOP_SPEAKING` 状态回调
- 改进了编码帧回调的参数类型，`onEncodedAudioFrameReceived`、`onEncodedVideoImageReceived`、`onEncodedVideoFrame` 现在使用 `ByteBuffer` 替代 `Byte` 数组

#### 改进与优化

- VAD 插件启动优化，`enableExtension` 现在在 SDK 内部实现，应用程序不再需要手动调用此方法
- 修复了 `VideoFrame` 中 `alphaBuffer` 和 `metadataBuffer` 的处理问题

### v4.4.30.1（2024-11-12）

#### API 变更

- 增加 AgoraAudioVad2 相关 `Vad2` 接口，移除 AgoraAudioVad 相关 `Vad` 接口
- 新增接收编码音频回调接口 `IAudioEncodedFrameObserver`

#### 改进与优化

- 修复 `LocalAudioDetailedStats` 相关回调崩溃问题
- 修改 `onAudioVolumeIndication` 回调参数类型

### v4.4.30（2024-10-24）

- 详细更新日志请参考 [发版说明](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes)

## 其他参考
详细参考官网（<https://doc.shengwang.cn/doc/rtc-server-sdk/java/landing-page>）

官网 API 文档 [Agora Server Java SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)


- 详细更新日志请参考 [发版说明](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes)

## 其他参考
详细参考官网（<https://doc.shengwang.cn/doc/rtc-server-sdk/java/landing-page>）

官网 API 文档 [Agora Server Java SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)

