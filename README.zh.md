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
  - [API 与功能模块](#api-与功能模块)
    - [API 文档参考](#api-文档参考)
    - [APM 功能](#apm-功能)
      - [使用模式](#使用模式)
      - [Local 模式](#local-模式)
        - [使用场景](#使用场景)
        - [核心类](#核心类)
          - [AgoraExternalAudioProcessor 类](#agoraexternalaudioprocessor-类)
          - [IExternalAudioProcessorObserver 接口](#iexternalaudioprocessorobserver-接口)
        - [场景一：仅使用 VAD](#场景一仅使用-vad)
        - [场景二：使用 VAD + 3A + BGHVS](#场景二使用-vad--3a--bghvs)
        - [完整示例](#完整示例)
      - [Remote 模式](#remote-模式)
        - [使用场景](#使用场景-1)
        - [核心配置](#核心配置)
          - [AgoraServiceConfig 配置](#agoraserviceconfig-配置)
          - [注册音频帧观察者](#注册音频帧观察者)
          - [IAudioFrameObserver 接口](#iaudioframeobserver-接口)
        - [场景一：仅使用 VAD](#场景一仅使用-vad-1)
        - [场景二：使用 VAD + 3A + BGHVS](#场景二使用-vad--3a--bghvs-1)
        - [完整示例](#完整示例-1)
      - [VAD 配置参数说明](#vad-配置参数说明)
        - [AgoraAudioVadConfigV2 属性](#agoraaudiovadconfigv2-属性)
        - [参数说明](#参数说明)
        - [VadProcessResult](#vadprocessresult)
    - [增量发送模式（Incremental Sending Mode）](#增量发送模式incremental-sending-mode)
      - [1. 适用场景与限制](#1-适用场景与限制)
      - [2. 行为机制](#2-行为机制)
      - [3. 参数说明 (SendExternalAudioParameters)](#3-参数说明-sendexternalaudioparameters)
      - [4. 接入示例](#4-接入示例)
  - [更新日志](#更新日志)
    - [v4.4.32.202 (2026-01-05)](#v4432202-2026-01-05)
    - [v4.4.32.201 (2025-12-18)](#v4432201-2025-12-18)
    - [v4.4.32.200 (2025-11-14)](#v4432200-2025-11-14)
    - [v4.4.32.101 (2025-09-01)](#v4432101-2025-09-01)
    - [v4.4.32.100 (2025-07-22)](#v4432100-2025-07-22)
    - [v4.4.32.1 (2025-06-12)](#v44321-2025-06-12)
    - [v4.4.32 (2025-05-27)](#v4432-2025-05-27)
    - [v4.4.31.4 (2025-03-21)](#v44314-2025-03-21)
    - [v4.4.31.3 (2025-02-26)](#v44313-2025-02-26)
    - [v4.4.31.2 (2025-02-19)](#v44312-2025-02-19)
    - [v4.4.31.1 (2025-01-06)](#v44311-2025-01-06)
    - [v4.4.31 (2024-12-23)](#v4431-2024-12-23)
    - [v4.4.30.2 (2024-11-20)](#v44302-2024-11-20)
    - [v4.4.30.1 (2024-11-12)](#v44301-2024-11-12)
    - [v4.4.30 (2024-10-24)](#v4430-2024-10-24)
  - [其他参考](#其他参考)

## 简介

Agora Linux Server Java SDK (v4.4.32.202) 为您提供了强大的实时音视频通信能力，可无缝集成到 Linux 服务器端 Java 应用程序中。借助此 SDK，您的服务器可以作为数据源或处理节点加入 Agora 频道，实时获取和处理音视频流，从而实现多种业务相关的其他高级功能。

> 注意：如果您是从 v4.4.32.100 之前的版本升级到 v4.4.32.100 及以后版本，请参考《[AIQoS 版本升级指南](./AIQoS_Upgrade_Guide.md)》完成必要的 API 适配与集成变更。

## 开发环境要求

### 硬件环境

- **操作系统**：Ubuntu 18.04+ 或 CentOS 8.0+
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
    <version>4.4.32.202</version>
</dependency>
```

### CDN 下载

[Agora-Linux-Java-SDK-v4.4.32.202-x86_64-994889-b202cdc4e3-20260105_103751](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.32.202-x86_64-994889-b202cdc4e3-20260105_103751.zip)

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
    <version>4.4.32.202</version>
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
  -Dversion=4.4.32.202 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

方法二：同时安装 SDK JAR 和 JavaDoc JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32.202 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -Djavadoc=sdk/agora-sdk-javadoc.jar
```

安装后，在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.202</version>
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
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-java-sdk/4.4.32.202/linux-java-sdk-4.4.32.202.jar
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
                ├── libagora-ffmpeg.so
                ├── libagora-soundtouch.so
                ├── libaosl.so
                ├── libbinding.so
                ├── libagora_ai_echo_cancellation_extension.so
                ├── libagora_ai_echo_cancellation_ll_extension.so
                ├── libagora_ai_noise_suppression_extension.so
                └── libagora_ai_noise_suppression_ll_extension.so
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

   ```bash
   sudo apt-get install maven -y
   sudo apt-get install lsof -y
   ```

2. **C++ 编译环境 (如果需要编译native库, 默认可以跳过)**

   安装基本编译工具：
   ```bash
   sudo apt-get update
   sudo apt-get install build-essential pkg-config gcc g++
   ```

3. **安装 C++ 运行时库**

   SDK 的原生库依赖于 `libc++` 运行时。请安装它以避免链接错误：
   ```bash
   sudo apt-get install libc++1
   ```

4. **FFmpeg 依赖 (如果需要编译FFmpeg相关功能, 默认可以跳过)**

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev libswresample-dev
   ```

5. **确保 JAVA_HOME 环境变量设置正确**

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

3. 运行时配置 (run_config)

   `run_config` 文件用于配置运行时的各种选项，位于 `Examples-Mvn/run_config`。您可以根据需要修改以下配置：

   | 配置项      | 类型    | 默认值 | 描述                                        |
   | ----------- | ------- | ------ | ------------------------------------------- |
   | enable_asan | boolean | false  | 是否启用 AddressSanitizer，用于内存错误检测 |

   **配置示例：**

   ```bash
   # 启用内存检查（调试模式）
   enable_asan=true
   ```

   > **注意**：
   > - 修改配置后需要重新编译项目

4. 配置Java SDK

本节介绍如何为您的Maven项目配置Linux Java SDK依赖。

4.1 步骤 1: 配置JAR包依赖
您有两种方式来配置项目的JAR依赖：

方式一：使用Maven中央仓库（推荐）
如果您的项目可以直接从Maven中央仓库获取依赖，请确保 `pom.xml` 中配置了正确的版本号。
```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.202</version>  <!-- 确保版本号与您需要使用的版本一致 -->
</dependency>
```

方式二：使用本地SDK包
如果您需要使用本地的SDK包（例如，经过定制化修改或内部版本），请遵循以下步骤：

1.  **放置SDK构件**: 将下载的SDK JAR包 (例如 `agora-sdk.jar`) 和对应的Javadoc包 (`agora-sdk-javadoc.jar`) 放置到 `Examples-Mvn/libs/` 目录下。

2.  **安装至本地Maven仓库**: 在 `linux_server_java` 目录下，执行以下脚本。该脚本会将 `libs` 目录下的JAR文件作为Maven构件安装到您的本地仓库中（通常位于 `~/.m2/repository`）。
    ```bash
    ./build_install_local_maven.sh
    ```

4.2 步骤 2: 配置原生库 (`.so`)
为了确保Java程序在运行时能成功加载原生库 (`.so` 文件)，需要将它们放置在指定的路径。

1.  **进入`libs`目录**:
    ```bash
    cd linux_server_java/Examples-Mvn/libs/
    ```

2.  **从JAR包中解压原生库**:
         ```bash
         # -x: extract, -v: verbose, -f: file
         jar -xvf agora-sdk.jar native/
         ```
    此命令会从 `agora-sdk.jar` 中提取 `native` 目录，其中包含了所有平台的原生库。

3.  **验证目录结构**:
    解压后，`libs` 目录结构应如下所示，确保 `.so` 文件位于 `native/linux/x86_64/` 路径下：
    ```text
    libs/
    ├── agora-sdk.jar
    ├── agora-sdk-javadoc.jar
    └── native/
        └── linux/
            └── x86_64/
                ├── libagora_rtc_sdk.so
                ├── libagora-fdkaac.so
                ├── libagora-ffmpeg.so
                ├── libagora-soundtouch.so
                ├── libaosl.so
                ├── libbinding.so
                ├── libagora_ai_echo_cancellation_extension.so
                ├── libagora_ai_echo_cancellation_ll_extension.so
                ├── libagora_ai_noise_suppression_extension.so
                └── libagora_ai_noise_suppression_ll_extension.so
    ```

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

- APM 功能

  参考 [ExternalAudioProcessorTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ExternalAudioProcessorTest.java),实现 APM 功能

## API 与功能模块

### API 文档参考

完整 API 文档可参考以下资源：

- [API-reference.zh.md](./API-reference.zh.md) 文件（仅供参考）
- 官方文档 [Agora Java Server SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)（以官方文档为准）

### APM 功能

APM (Audio Processing Module) 功能提供了服务端的回声消除 (AEC)、降噪 (ANS)、自动增益 (AGC) 和背景人声消除 (BGHVS) 等音频处理能力。

> **注意**：通常情况下，AEC/ANS/AGC 等功能已经在客户端 SDK 中实现，服务端不需要再次处理，除非有特殊的业务需求（例如处理来自非声网 SDK 客户端的原始音频流）。

如果您确定需要启用服务端的 APM 功能，请联系声网技术支持以获取详细指导和配置说明。

#### 使用模式

APM 功能支持两种使用模式：

| 模式        | 说明                                                    | 是否需要加入频道 |
| ----------- | ------------------------------------------------------- | ---------------- |
| Local 模式  | 本地音频处理，不需要加入频道，直接推送 PCM 数据进行处理 | 否               |
| Remote 模式 | 远端音频处理，需要加入频道，处理来自频道的音频流        | 是               |

#### Local 模式

Local 模式适用于不需要加入频道的场景，直接推送本地 PCM 音频数据进行 VAD 和/或 3A+BGHVS 处理。

##### 使用场景

| 场景                  | vadConfig | apmConfig | 说明                           |
| --------------------- | --------- | --------- | ------------------------------ |
| 仅使用 VAD            | 不为 null | null      | 仅进行语音活动检测             |
| 使用 VAD + 3A + BGHVS | 不为 null | 不为 null | 同时进行语音活动检测和音频处理 |

##### 核心类

###### AgoraExternalAudioProcessor 类

外部音频处理器，用于处理音频数据的 3A（AEC、ANS、AGC）和 VAD。

**创建方法**

```java
AgoraExternalAudioProcessor audioProcessor = service.createExternalAudioProcessor();
```

**初始化方法**

```java
public int initialize(AgoraApmConfig apmConfig, int outputSampleRate, int outputChannels,
        AgoraAudioVadConfigV2 vadConfig, IExternalAudioProcessorObserver observer)
```

- **参数**
  - `apmConfig`：`AgoraApmConfig` 类型，APM 配置。设置为 `null` 表示不启用 3A+BGHVS 处理
  - `outputSampleRate`：输出采样率
  - `outputChannels`：输出声道数
  - `vadConfig`：`AgoraAudioVadConfigV2` 类型，VAD 配置。设置为 `null` 表示不启用 VAD
  - `observer`：`IExternalAudioProcessorObserver` 类型，音频帧回调观察者
- **返回**
  - 0 表示成功，负值表示失败

**推送音频数据方法**

```java
public int pushAudioPcmData(byte[] data, int sampleRate, int channels, long presentationMs)
```

- **参数**
  - `data`：PCM 音频数据（16-bit）
  - `sampleRate`：采样率
  - `channels`：声道数
  - `presentationMs`：音频帧的 PTS（毫秒）
- **返回**
  - 0 表示成功，负值表示失败

**销毁方法**

```java
public void destroy()
```

###### IExternalAudioProcessorObserver 接口

外部音频处理器观察者接口，用于接收处理后的音频帧。

```java
public interface IExternalAudioProcessorObserver {
    void onAudioFrame(AgoraExternalAudioProcessor audioProcessor, AudioFrame audioFrame, VadProcessResult vadProcessResult);
}
```

- **参数**
  - `audioProcessor`：生成此回调的音频处理器实例，当多个处理器共用同一个观察者时用于区分来源
  - `audioFrame`：处理后的音频帧
  - `vadProcessResult`：VAD 处理结果，包含 VAD 状态和输出帧

##### 场景一：仅使用 VAD

仅进行语音活动检测，不启用 3A+BGHVS 处理。

```java
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraExternalAudioProcessor;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.IExternalAudioProcessorObserver;
import io.agora.rtc.VadProcessResult;

public class VadOnlyExample {
    public static void main(String[] args) {
        // 1. 初始化 AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);
        service.initialize(config);

        // 2. 创建外部音频处理器
        AgoraExternalAudioProcessor audioProcessor = service.createExternalAudioProcessor();

        // 3. 配置 VAD（参考 VAD 配置参数说明）
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // 使用默认值，可根据需要调整

        // 4. 初始化处理器（apmConfig 设置为 null，仅使用 VAD）
        int ret = audioProcessor.initialize(
            null,  // apmConfig 为 null，不启用 3A+BGHVS
            16000, // 输出采样率
            1,     // 输出声道数
            vadConfig,
            new IExternalAudioProcessorObserver() {
                @Override
                public void onAudioFrame(AgoraExternalAudioProcessor audioProcessor, AudioFrame audioFrame, VadProcessResult vadProcessResult) {
                    // audioProcessor 可用于区分回调来自哪个处理器（多处理器共用观察者时）
                    // 处理回调
                    if (vadProcessResult != null) {
                        System.out.println("VAD State: " + vadProcessResult.getState());
                        // vadProcessResult.getOutFrame() 包含 VAD 处理后的音频数据
                    }
                }
            }
        );

        if (ret != 0) {
            System.err.println("Initialize failed: " + ret);
            return;
        }

        // 5. 推送 PCM 音频数据（10ms 一帧，16kHz 单声道 = 320 字节）
        byte[] pcmData = new byte[320]; // 16000 * 1 * 2 / 100 = 320 bytes per 10ms
        // ... 填充 PCM 数据 ...
        long presentationMs = 0;
        audioProcessor.pushAudioPcmData(pcmData, 16000, 1, presentationMs);

        // 6. 销毁资源
        audioProcessor.destroy();
        service.destroy();
    }
}
```

##### 场景二：使用 VAD + 3A + BGHVS

同时进行语音活动检测和 3A+BGHVS 音频处理。

```java
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraExternalAudioProcessor;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.IExternalAudioProcessorObserver;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.apm.AgoraApmConfig;

public class VadWith3AExample {
    public static void main(String[] args) {
        // 1. 初始化 AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);
        service.initialize(config);

        // 2. 创建外部音频处理器
        AgoraExternalAudioProcessor audioProcessor = service.createExternalAudioProcessor();

        // 3. 配置 APM（3A + BGHVS）
        AgoraApmConfig apmConfig = new AgoraApmConfig();
        // 默认已启用 AEC、ANS、AGC、BGHVS，可根据需要调整
        // apmConfig.getAiAecConfig().setEnabled(true);   // 回声消除
        // apmConfig.getAiNsConfig().setNsEnabled(true);  // 降噪
        // apmConfig.getAgcConfig().setEnabled(true);     // 自动增益
        // apmConfig.getBghvsConfig().setEnabled(true);   // 背景人声消除
        // apmConfig.setEnableDump(false);                // 调试时可开启 dump

        // 4. 配置 VAD（参考 VAD 配置参数说明）
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // 使用默认值，可根据需要调整

        // 5. 初始化处理器（同时启用 apmConfig 和 vadConfig）
        int ret = audioProcessor.initialize(
            apmConfig,  // 启用 3A+BGHVS
            16000,      // 输出采样率
            1,          // 输出声道数
            vadConfig,  // 启用 VAD
            new IExternalAudioProcessorObserver() {
                @Override
                public void onAudioFrame(AgoraExternalAudioProcessor audioProcessor, AudioFrame audioFrame, VadProcessResult vadProcessResult) {
                    // audioProcessor 可用于区分回调来自哪个处理器（多处理器共用观察者时）
                    // audioFrame 包含经过 3A+BGHVS 处理后的音频数据
                    if (audioFrame != null) {
                        byte[] processedData = io.agora.rtc.utils.Utils.getBytes(audioFrame.getBuffer());
                        // ... 使用处理后的音频数据 ...
                    }
                    
                    // vadProcessResult 包含 VAD 处理结果
                    if (vadProcessResult != null) {
                        System.out.println("VAD State: " + vadProcessResult.getState());
                        // vadProcessResult.getOutFrame() 包含 VAD 处理后的音频数据
                    }
                }
            }
        );

        if (ret != 0) {
            System.err.println("Initialize failed: " + ret);
            return;
        }

        // 6. 推送 PCM 音频数据（10ms 一帧，16kHz 单声道 = 320 字节）
        byte[] pcmData = new byte[320];
        // ... 填充 PCM 数据 ...
        long presentationMs = 0;
        audioProcessor.pushAudioPcmData(pcmData, 16000, 1, presentationMs);

        // 7. 销毁资源
        audioProcessor.destroy();
        service.destroy();
    }
}
```

##### 完整示例

完整的示例代码请参考：`Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ExternalAudioProcessorTest.java`

#### Remote 模式

Remote 模式适用于需要加入频道的场景，接收并处理来自频道的远端音频流，同时进行 VAD 和/或 3A+BGHVS 处理。

##### 使用场景

| 场景                  | AgoraServiceConfig.apmConfig | registerAudioFrameObserver vadConfig | 说明                           |
| --------------------- | ---------------------------- | ------------------------------------ | ------------------------------ |
| 仅使用 VAD            | null 或不设置                | 不为 null                            | 仅进行语音活动检测             |
| 使用 VAD + 3A + BGHVS | 不为 null                    | 不为 null                            | 同时进行语音活动检测和音频处理 |

##### 核心配置

###### AgoraServiceConfig 配置

在初始化 `AgoraService` 时，通过 `AgoraServiceConfig` 配置 APM：

```java
AgoraServiceConfig config = new AgoraServiceConfig();
config.setApmMode(Constants.ApmMode.ENABLE);  // 启用 APM 模式

// 配置 3A+BGHVS（可选，设置为 null 表示不启用）
AgoraApmConfig apmConfig = new AgoraApmConfig();
config.setApmConfig(apmConfig);  // 设置为 null 表示不启用 3A+BGHVS
```

###### 注册音频帧观察者

通过 `registerAudioFrameObserver` 方法注册音频帧观察者并配置 VAD：

```java
conn.registerAudioFrameObserver(audioFrameObserver, true, new AgoraAudioVadConfigV2());
```

- **参数**
  - `observer`：`IAudioFrameObserver` 类型，音频帧回调观察者
  - `enableVad`：是否启用 VAD
  - `vadConfig`：`AgoraAudioVadConfigV2` 类型，VAD 配置

###### IAudioFrameObserver 接口

音频帧观察者接口，用于接收处理后的远端音频帧。

```java
public interface IAudioFrameObserver {
    int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
            String channelId, String userId, AudioFrame frame, VadProcessResult vadResult);
}
```

- **参数**
  - `agoraLocalUser`：本地用户对象
  - `channelId`：频道 ID
  - `userId`：远端用户 ID
  - `frame`：处理后的音频帧（经过 3A+BGHVS 处理，如果启用）
  - `vadResult`：VAD 处理结果，包含 VAD 状态和输出帧

##### 场景一：仅使用 VAD

仅进行语音活动检测，不启用 3A+BGHVS 处理。

```java
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.VadProcessResult;

public class RemoteVadOnlyExample {
    public static void main(String[] args) {
        // 1. 初始化 AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);
        // 不设置 apmConfig，表示不启用 3A+BGHVS
        // config.setApmConfig(null);
        service.initialize(config);

        // 2. 创建连接
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, null);

        // 3. 订阅音频
        conn.getLocalUser().subscribeAllAudio();

        // 4. 设置音频参数
        conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(1, 16000);

        // 5. 注册音频帧观察者（启用 VAD）
        IAudioFrameObserver audioFrameObserver = new IAudioFrameObserver() {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
                    String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
                if (frame == null) {
                    return 0;
                }
                
                // 处理音频帧
                byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(frame.getBuffer());
                
                // 处理 VAD 结果
                if (vadResult != null) {
                    System.out.println("VAD State: " + vadResult.getState());
                    if (vadResult.getState() == Constants.VadState.START_SPEAKING
                            || vadResult.getState() == Constants.VadState.SPEAKING
                            || vadResult.getState() == Constants.VadState.STOP_SPEAKING) {
                        byte[] vadData = vadResult.getOutFrame();
                        // ... 使用 VAD 处理后的音频数据 ...
                    }
                }
                return 1;
            }
        };

        // 配置 VAD（参考 VAD 配置参数说明）
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // 使用默认值，可根据需要调整
        conn.registerAudioFrameObserver(audioFrameObserver, true, vadConfig);

        // 6. 加入频道
        conn.connect("your_token", "channel_id", "user_id");

        // ... 业务逻辑 ...

        // 7. 离开频道并销毁资源
        conn.disconnect();
        conn.destroy();
        service.destroy();
    }
}
```

##### 场景二：使用 VAD + 3A + BGHVS

同时进行语音活动检测和 3A+BGHVS 音频处理。

```java
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.apm.AgoraApmConfig;

public class RemoteVadWith3AExample {
    public static void main(String[] args) {
        // 1. 初始化 AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);

        // 配置 APM（3A + BGHVS）
        AgoraApmConfig apmConfig = new AgoraApmConfig();
        // 默认已启用 AEC、ANS、AGC、BGHVS，可根据需要调整
        config.setApmConfig(apmConfig);  // 启用 3A+BGHVS

        service.initialize(config);

        // 2. 创建连接
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, null);

        // 3. 订阅音频
        conn.getLocalUser().subscribeAllAudio();

        // 4. 设置音频参数
        conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(1, 16000);

        // 5. 注册音频帧观察者（启用 VAD）
        IAudioFrameObserver audioFrameObserver = new IAudioFrameObserver() {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
                    String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
                if (frame == null) {
                    return 0;
                }
                
                // frame 包含经过 3A+BGHVS 处理后的音频数据
                byte[] processedData = io.agora.rtc.utils.Utils.getBytes(frame.getBuffer());
                // ... 使用处理后的音频数据 ...
                
                // 处理 VAD 结果
                if (vadResult != null) {
                    System.out.println("VAD State: " + vadResult.getState());
                    if (vadResult.getState() == Constants.VadState.START_SPEAKING
                            || vadResult.getState() == Constants.VadState.SPEAKING
                            || vadResult.getState() == Constants.VadState.STOP_SPEAKING) {
                        byte[] vadData = vadResult.getOutFrame();
                        // ... 使用 VAD 处理后的音频数据 ...
                    }
                }
                return 1;
            }
        };

        // 配置 VAD（参考 VAD 配置参数说明）
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // 使用默认值，可根据需要调整
        conn.registerAudioFrameObserver(audioFrameObserver, true, vadConfig);

        // 6. 加入频道
        conn.connect("your_token", "channel_id", "user_id");

        // ... 业务逻辑 ...

        // 7. 离开频道并销毁资源
        conn.disconnect();
        conn.destroy();
        service.destroy();
    }
}
```

##### 完整示例

完整的示例代码请参考：`Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmVadTest.java`

#### VAD 配置参数说明

VAD 配置使用 `AgoraAudioVadConfigV2` 类进行配置。

##### AgoraAudioVadConfigV2 属性

| 属性名                     | 类型    | 描述                                       | 默认值 | 取值范围               |
| -------------------------- | ------- | ------------------------------------------ | ------ | ---------------------- |
| preStartRecognizeCount     | int     | 开始说话状态前保存的音频帧数               | 16     | [0, Integer.MAX_VALUE] |
| startRecognizeCount        | int     | 说话状态的音频帧数                         | 30     | [1, Integer.MAX_VALUE] |
| stopRecognizeCount         | int     | 停止说话状态的音频帧数                     | 65     | [1, Integer.MAX_VALUE] |
| activePercent              | float   | 在 startRecognizeCount 帧中活跃帧的百分比  | 0.7    | [0.0, 1.0]             |
| inactivePercent            | float   | 在 stopRecognizeCount 帧中非活跃帧的百分比 | 0.5    | [0.0, 1.0]             |
| startVoiceProb             | int     | 开始语音检测的概率阈值                     | 70     | [0, 100]               |
| stopVoiceProb              | int     | 停止语音检测的概率阈值                     | 70     | [0, 100]               |
| startRmsThreshold          | int     | 开始语音检测的 RMS 阈值 (dB)               | -70    | [-100, 0]              |
| stopRmsThreshold           | int     | 停止语音检测的 RMS 阈值 (dB)               | -70    | [-100, 0]              |
| enableAdaptiveRmsThreshold | boolean | 是否启用自适应 RMS 阈值                    | true   | true/false             |
| adaptiveRmsThresholdFactor | float   | 自适应 RMS 阈值因子                        | 0.67   | [0.0, 1.0]             |

##### 参数说明

**窗口大小参数**：
- `preStartRecognizeCount`: 预启动缓冲帧数，用于保留语音开头部分（16帧 = 160ms）
- `startRecognizeCount`: 启动检测窗口大小，判断是否开始说话（30帧 = 300ms）
- `stopRecognizeCount`: 停止检测窗口大小，判断是否停止说话（65帧 = 650ms）

**百分比阈值**：
- `activePercent`: 启动活动帧比例阈值（默认0.7 = 70%），值越高启动越严格
- `inactivePercent`: 停止非活动帧比例阈值（默认0.5 = 50%），值越高停止越快

**语音概率阈值**：
- `startVoiceProb`: 启动语音概率阈值（0-100），值越低越敏感，更早开始检测
- `stopVoiceProb`: 停止语音概率阈值（0-100），值越高越早停止检测

**RMS 能量阈值**：
- `startRmsThreshold`: 启动 RMS 阈值（dB），值越高对语音越敏感
  - 安静环境：-70 dB（默认）
  - 噪声环境：-50 到 -40 dB
  - 高噪声环境：-40 到 -30 dB
- `stopRmsThreshold`: 停止 RMS 阈值（dB），通常与 startRmsThreshold 保持一致

**自适应阈值**：
- `enableAdaptiveRmsThreshold`: 启用后根据历史语音统计自动调整 RMS 阈值，提高环境适应性
- `adaptiveRmsThresholdFactor`: 自适应因子（默认0.67 = 2/3），值越小阈值越低越敏感

##### VadProcessResult

存储 VAD 处理结果。

```java
public VadProcessResult(byte[] result, Constants.VadState state)
```

- **参数**
  - `result`：`byte[]` 类型，处理后的音频数据。
  - `state`：`Constants.VadState` 类型，当前 VAD 状态。

**VAD 状态说明**：
- `UNKNOWN`：未知状态
- `NOT_SPEAKING`：未说话
- `START_SPEAKING`：开始说话
- `SPEAKING`：正在说话
- `STOP_SPEAKING`：停止说话

### 增量发送模式（Incremental Sending Mode）

该模式专为 AI 场景（特别是 TTS 业务）设计，允许开发者在连接建立初期以受控的高倍率推送外部音频数据，从而平衡首帧延迟与链路稳定性。

#### 1. 适用场景与限制
- **核心场景**：仅推荐在 IoT 等对首字延迟敏感的 TTS 场景下启用。
- **作用域**：配置作用于 **Connection（连接）** 粒度，支持不同连接差异化设置。

#### 2. 行为机制
- **突发加速**：在设定的“加速窗口期（sendMs）”内，SDK 将按照指定的“倍率（sendSpeed）”加速推送缓存或实时生成的音频帧。
- **平滑回落**：窗口期结束后，自动回退至 1x 正常速率发送，确保后续实时流的平稳传输。

#### 3. 参数说明 (SendExternalAudioParameters)

| 参数 | 类型 | 默认值 | 推荐值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| **enabled** | boolean | false | true | 是否激活增量发送策略。 |
| **sendMs** | int | 0 | 500 | **加速窗口期**（毫秒）。定义连接建立后执行加速发送的持续时长。 |
| **sendSpeed** | int | 0 | 2 | **加速倍率**。取值范围 [1, 5]。推荐设为 2，即以 2 倍速消耗数据。 |
| **deliverMuteDataForFakeAdmin** | boolean | false | false | **引擎级静音补帧**。无数据时是否发送静音包。<br>⚠️ **注意**：此参数为 `AgoraService` 引擎级别全局生效，非单连接配置。 |

#### 4. 接入示例

```java
// connection 连接配置
RtcConnPublishConfig publishConfig = new RtcConnPublishConfig();

// 1.设置音频场景为 AI_SERVER 或者 DEFAULT
publishConfig.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);

SendExternalAudioParameters params = new SendExternalAudioParameters();
params.setEnabled(true);      // 激活功能
params.setSendMs(500);        // 前 500ms 执行加速
params.setSendSpeed(2);       // 使用 2 倍速发送
// 2.初始化增量发送配置
publishConfig.setSendExternalAudioParameters(params);
// ... 其他配置
```

## 更新日志

### v4.4.32.202 (2026-01-05)

- **API 变更**
  - **RtcConnPublishConfig**:
    - 新增 `setSendExternalAudioParameters` 方法，用于配置“增量发送模式（Incremental Sending Mode）”。
    - 该模式通过 `SendExternalAudioParameters` 类进行参数化配置，详情请参阅文档上方 [增量发送模式](#增量发送模式incremental-sending-mode) 章节。
  - **AgoraRtcConn**:
    - 新增 `sendIntraRequest` 方法，支持主动向发送端请求关键帧（I 帧），用于优化视频弱网恢复体验。

### v4.4.32.201 (2025-12-18)

- **API 变更**
  - **AgoraServiceConfig**: 
    - 将 `enableApm` 属性更改为 `apmMode`，类型从 `boolean` 改为 `Constants.ApmMode` 枚举。
    - 新增 `Constants.ApmMode` 枚举，支持 `DISABLE` 和 `ENABLE` 两种模式。
    - 默认值从 `true` 改为 `Constants.ApmMode.DISABLE`。
  - **AgoraExternalAudioProcessor**: 
    - 新增外部音频处理器类，支持本地 APM 模式的音频处理。
    - 提供 PCM 音频数据推送和处理后音频接收的能力。
  - **IExternalAudioProcessorObserver**: 
    - 新增外部音频处理器观察者接口，用于接收处理后的音频帧回调。

- **改进与优化**
  - 修复了内存泄漏问题，优化了模型类的销毁流程。

### v4.4.32.200 (2025-11-14)

- **API 变更**
  - **AgoraServiceConfig**: 新增 `enableApm` 和 `apmConfig` 属性，支持配置 APM（Audio Processing Module）模块。

- **改进与优化**
  - 优化了 VAD V2 的默认配置参数，提升语音活动检测的准确性。
  - 修复了 Audio 和 Video 回调参数中pts值的准确性问题。

### v4.4.32.101 (2025-09-01)

- **API 变更**
  - **AudioFrame**: 新增 `presentationMs` 字段及 getter/setter，用于透传音频帧 PTS（毫秒）。
  - **EncodedVideoFrameInfo**: 新增 `presentationMs` 字段及构造参数；可用于传入视频帧 PTS（毫秒）。
  - **EncodedAudioFrameInfo**: 新增 `captureTimeMs` 字段及构造参数；用于记录采集时间戳（毫秒）。
  - **AgoraRtcConn**: 新增重载 `pushAudioPcmData(byte[] data, int sampleRate, int channels, long presentationMs)`；原有 `pushAudioPcmData(byte[] data, int sampleRate, int channels)` 仍可用（等价于 `presentationMs=0`）。

- **改进与优化**
  - 修复 `IAudioFrameObserver` 回调在极端场景下可能出现的异常问题，提升回调稳定性。

### v4.4.32.100 (2025-07-22)

- **API 变更**
  - 该版本支持AIQoS，同时API有变更，请参考[AIQoS升级指南](AIQoS_Upgrade_Guide.md)

### v4.4.32.1 (2025-06-12)

- **API 变更**
  - 优化 `ILocalUserObserver` 接口的 `onStreamMessage` 回调参数，将原有的 `onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data, long length)` 修改为 `onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, byte[] data)`，提升消息处理的灵活性和效率。

- **改进与优化**
  - 修复了 `AgoraServiceConfig` 的 `setLogFileSize` 方法，单位为 KB 时实际生效单位为 Byte 的问题，现已正确按 KB 设置日志文件大小。

### v4.4.32 (2025-05-27)

- **API 变更**
  - `AgoraService` 新增 `getSdkVersion` 方法，用于获取 SDK 版本号
  - `AgoraAudioEncodedFrameSender` 移除 `send(byte[] payloadData, int payloadSize, EncodedAudioFrameInfo info)` 方法，新增 `sendEncodedAudioFrame(byte[] payloadData, EncodedAudioFrameInfo info)` 方法替代
  - `AgoraAudioPcmDataSender` 的 `send(byte[] audioData, int captureTimestamp, int samplesPerChannel, int bytesPerSample,
int numberOfChannels, int sampleRate) ` 方法标位不推荐，新增 `sendAudioPcmData(AudioFrame audioFrame)` 方法替代
  - `AgoraVideoEncodedImageSender` 移除 `send(byte[] imageBuffer, int length, EncodedVideoFrameInfo info` 方法，新增 `sendEncodedVideoImage(byte[] imageBuffer, EncodedVideoFrameInfo info)` 方法替代
  - `AgoraVideoFrameSender` 移除 `send(ExternalVideoFrame frame)` 方法，新增 `sendVideoFrame(ExternalVideoFrame frame)` 方法替代

- **改进与优化**
  - 修复了 `destroy` 方法可能导致的崩溃问题

### v4.4.31.4 (2025-03-21)

- **改进与优化**
  - 修复了多线程环境下可能导致的异常崩溃问题
  - 改进了错误处理流程，增强了异常情况下的恢复能力

### v4.4.31.3 (2025-02-26)

- **改进与优化**
- 修复了由于内存复用可能导致的异常处理问题

### v4.4.31.2 (2025-02-19)

- **API 变更**
  - 新增 `sendStreamMessage(int streamId, byte[] messageData)` 方法，弃用 `sendStreamMessage(int streamId, String message, int length)` 方法

- **改进与优化**
  - 优化代码处理，提高系统稳健性

### v4.4.31.1 (2025-01-06)

- **改进与优化**
  - 优化 VAD 功能配置，现在默认开启 VAD 功能，无需手动配置

### v4.4.31 (2024-12-23)

- **API 变更**
  - 在 `AgoraServiceConfig` 中新增 `DomainLimit` 配置选项，用于域名限制管理
  - 新增 `VadDumpUtils` 工具类，支持导出 VAD 处理过程的调试数据
  - 新增 `AudioConsumerUtils` 类，提供优化的 PCM 数据传输机制
  - 在 `AgoraLocalUser` 中修改 `registerAudioFrameObserver` 方法，支持配置 `AgoraAudioVadConfigV2` 参数
  - 在 `IAudioFrameObserver` 中新增 `onPlaybackAudioFrameBeforeMixing` 回调的 `vadResult` 参数
  - 在 `AgoraLocalUser` 类中新增 `sendAudioMetaData` 方法，支持发送音频元数据
  - 在 `ILocalUserObserver` 类中新增 `onAudioMetaDataReceived` 回调，用于接收音频元数据
  - 在 `ExternalVideoFrame` 类中增加 `ColorSpace` 属性，支持自定义颜色空间设置

- **改进与优化**
  - 优化代码逻辑架构，显著提升内存使用效率
  - 修复多处内存泄露问题，提高系统稳定性
  - 增强内存访问安全机制，有效防止内存踩踏问题

### v4.4.30.2 (2024-11-20)

**API 变更**
  - 增强了 AgoraAudioVadV2 的 `processFrame` 处理，新增 `START_SPEAKING` 和 `STOP_SPEAKING` 状态回调
  - 改进了编码帧回调的参数类型，`onEncodedAudioFrameReceived`、`onEncodedVideoImageReceived`、`onEncodedVideoFrame` 现在使用 `ByteBuffer` 替代 `Byte` 数组

- **改进与优化**
  - VAD 插件启动优化，`enableExtension` 现在在 SDK 内部实现，应用程序不再需要手动调用此方法
  - 修复了 `VideoFrame` 中 `alphaBuffer` 和 `metadataBuffer` 的处理问题

### v4.4.30.1 (2024-11-12)

- **API 变更**
  - 增加 AgoraAudioVad2 相关 `Vad2` 接口，移除 AgoraAudioVad 相关 `Vad` 接口
  - 新增接收编码音频回调接口 `IAudioEncodedFrameObserver`

- **改进与优化**
  - 修复 `LocalAudioDetailedStats` 相关回调崩溃问题
  - 修改 `onAudioVolumeIndication` 回调参数类型

### v4.4.30 (2024-10-24)

- 详细更新日志请参考 [发版说明](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes)

## 其他参考
- 详细参考官网（<https://doc.shengwang.cn/doc/rtc-server-sdk/java/landing-page>）

- 官网 API 文档 [Agora Server Java SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)
