# Agora Linux Server Java SDK

[中文](./README.zh.md) | English

## Table of Contents

- [Agora Linux Server Java SDK](#agora-linux-server-java-sdk)
  - [Table of Contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Development Environment Requirements](#development-environment-requirements)
    - [Hardware Environment](#hardware-environment)
    - [Software Environment](#software-environment)
  - [SDK Download](#sdk-download)
    - [Maven Download](#maven-download)
    - [CDN Download](#cdn-download)
  - [Integrate the SDK](#integrate-the-sdk)
    - [1. Maven Integration](#1-maven-integration)
      - [1.1 Add Maven Dependency](#11-add-maven-dependency)
      - [1.2 Integrate .so Library Files](#12-integrate-so-library-files)
    - [2. Local SDK Integration](#2-local-sdk-integration)
      - [2.1 SDK Package Structure](#21-sdk-package-structure)
      - [2.2 Integrate JAR Files](#22-integrate-jar-files)
          - [Local Maven Repository Method](#local-maven-repository-method)
          - [Direct Reference Method](#direct-reference-method)
      - [2.3 Integrate .so Library Files](#23-integrate-so-library-files)
    - [3. Loading Native Libraries (.so Files)](#3-loading-native-libraries-so-files)
      - [3.1 Extract .so Library Files](#31-extract-so-library-files)
      - [3.2 Configure Loading Path](#32-configure-loading-path)
  - [Quick Start](#quick-start)
    - [Official Example Documentation](#official-example-documentation)
    - [Enable Service](#enable-service)
    - [Run Examples-Mvn Project](#run-examples-mvn-project)
      - [Environment Preparation](#environment-preparation)
      - [Project Configuration](#project-configuration)
      - [Build Process](#build-process)
      - [Running Examples](#running-examples)
      - [Test Cases](#test-cases)
  - [API and Feature Modules](#api-and-feature-modules)
    - [API Documentation Reference](#api-documentation-reference)
    - [APM Features](#apm-features)
      - [Usage Modes](#usage-modes)
      - [Local Mode](#local-mode)
        - [Usage Scenarios](#usage-scenarios)
        - [Core Classes](#core-classes)
          - [AgoraExternalAudioProcessor Class](#agoraexternalaudioprocessor-class)
          - [IExternalAudioProcessorObserver Interface](#iexternalaudioprocessorobserver-interface)
        - [Scenario 1: VAD Only](#scenario-1-vad-only)
        - [Scenario 2: VAD + 3A + BGHVS](#scenario-2-vad--3a--bghvs)
        - [Complete Example](#complete-example)
      - [Remote Mode](#remote-mode)
        - [Usage Scenarios](#usage-scenarios-1)
        - [Core Configuration](#core-configuration)
          - [AgoraServiceConfig Configuration](#agoraserviceconfig-configuration)
          - [Register Audio Frame Observer](#register-audio-frame-observer)
          - [IAudioFrameObserver Interface](#iaudioframeobserver-interface)
        - [Scenario 1: VAD Only](#scenario-1-vad-only-1)
        - [Scenario 2: VAD + 3A + BGHVS](#scenario-2-vad--3a--bghvs-1)
        - [Complete Example](#complete-example-1)
      - [VAD Configuration Parameters](#vad-configuration-parameters)
        - [AgoraAudioVadConfigV2 Properties](#agoraaudiovadconfigv2-properties)
        - [Parameter Description](#parameter-description)
        - [VadProcessResult](#vadprocessresult)
  - [Changelog](#changelog)
    - [v4.4.32.201 (2025-12-18)](#v4432201-2025-12-18)
    - [v4.4.32.200 (2025-11-14)](#v4432200-2025-11-14)
    - [v4.4.32.101 (2025-09-01)](#v4432101-2025-09-01)
    - [v4.4.32.100（2025-07-22）](#v44321002025-07-22)
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
  - [Other References](#other-references)

## Introduction

The Agora Linux Server Java SDK (v4.4.32.201) provides powerful real-time audio and video communication capabilities that can be seamlessly integrated into Linux server-side Java applications. With this SDK, your server can join Agora channels as a data source or processing node, accessing and processing audio and video streams in real-time to implement various business-related advanced features.

> Note: If you are upgrading from a version earlier than v4.4.32.100 to v4.4.32.100 or later, please refer to the [AIQoS Upgrade Guide](./AIQoS_Upgrade_Guide.md) for required API and integration changes.

## Development Environment Requirements

### Hardware Environment

- **Operating System**: Ubuntu 18.04+ or CentOS 8.0+
- **CPU Architecture**: x86-64
- **Performance Requirements**:
  - CPU: 8 cores @ 1.8 GHz or higher
  - Memory: 2 GB (4 GB+ recommended)
- **Network Requirements**:
  - Public IP address
  - Allow access to `.agora.io` and `.agoralab.co` domains

### Software Environment

- Apache Maven or other build tools
- JDK 8+

## SDK Download

### Maven Download

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.201</version>
</dependency>
```

### CDN Download

[Agora-Linux-Java-SDK-v4.4.32.201-x86_64-994889-3c3167f90e-20251218_102056](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.32.201-x86_64-994889-3c3167f90e-20251218_102056.zip)

## Integrate the SDK

There are two ways to integrate the SDK: via Maven integration and local SDK integration.

### 1. Maven Integration

Maven integration is the simplest method, automatically managing Java dependencies.

#### 1.1 Add Maven Dependency

Add the following dependency to your project's `pom.xml` file:

```xml
<!-- x86_64 platform -->
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.201</version>
</dependency>
```

#### 1.2 Integrate .so Library Files

The Maven dependency includes the required JAR files, but you still need to manually handle the `.so` library files to run the application. Please refer to the **Loading Native Libraries (.so Files)** section below.

### 2. Local SDK Integration

The local SDK is a complete package containing all necessary files, suitable for scenarios requiring more flexible control.

#### 2.1 SDK Package Structure

The SDK package (zip format) downloaded from the official website contains the following:

- **doc/** - JavaDoc documentation, detailed API descriptions
- **examples/** - Example code and projects
- **sdk/** - Core SDK files
  - `agora-sdk.jar` - Java library
  - `agora-sdk-javadoc.jar` - JavaDoc documentation

#### 2.2 Integrate JAR Files

You can integrate the JAR files in two ways:

###### Local Maven Repository Method

Method 1: Install only the SDK JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32.201 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

Method 2: Install both SDK JAR and JavaDoc JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32.201 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -Djavadoc=sdk/agora-sdk-javadoc.jar
```

After installation, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.201</version>
</dependency>
```

###### Direct Reference Method

1. Copy the JAR files to your project's `libs` directory:

   ```sh
   mkdir -p libs
   cp sdk/agora-sdk.jar libs/
   cp sdk/agora-sdk-javadoc.jar libs/  # Optional, for IDE support
   ```

2. Add a classpath reference in your Java project:

   ```sh
   # Use the SDK JAR
   java -cp .:libs/agora-sdk.jar YourMainClass

   # Configure JavaDoc in your IDE (Common IDEs like IntelliJ IDEA or Eclipse support direct association of JavaDoc JARs)
   ```

#### 2.3 Integrate .so Library Files

The downloaded SDK package already contains the `.so` files. You need to ensure that the Java program can find these files at runtime. Please refer to the **Loading Native Libraries (.so Files)** section below.

### 3. Loading Native Libraries (.so Files)

The Agora Linux Server Java SDK depends on underlying C++ native libraries (`.so` files). Whether integrating via Maven or locally, you must ensure the Java Virtual Machine (JVM) can find and load these libraries at runtime.

#### 3.1 Extract .so Library Files

The `.so` files are contained within the `agora-sdk.jar` or `linux-java-sdk-x.x.x.x.jar` file. You need to extract them first:

1.  Create a directory in your project or deployment location to store the library files, for example, `libs`:

    ```sh
    mkdir -p libs
    cd libs
    ```

2.  Use the `jar` command to extract the contents from the SDK's JAR file (assuming the JAR file is in the `libs` directory or Maven cache):

    ```sh
    # If using local integration, the JAR file is typically in the libs directory
    jar xvf agora-sdk.jar

    # If using Maven integration, the JAR file is in the Maven cache, e.g.:
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-java-sdk/4.4.32.201/linux-java-sdk-4.4.32.201.jar
    ```

3.  After extraction, a `native/linux/x86_64` subdirectory will be generated within the `libs` directory, containing the required `.so` files:

    ```
    libs/
    ├── agora-sdk.jar (or empty, if only used for extraction)
    ├── io/          # Java class files, ignore
    ├── META-INF/    # JAR and application metadata, ignore
    └── native/      # Native libraries for corresponding platforms
        └── linux/
            └── x86_64/   # x86_64 platform .so libraries
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

#### 3.2 Configure Loading Path

There are two main methods to help the JVM find the `.so` files:

**Method 1: Setting the `LD_LIBRARY_PATH` Environment Variable (Recommended)**

This is the most reliable method, especially when there are dependencies between `.so` files.

```sh
# Determine the directory containing your .so files, assuming ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# Set the LD_LIBRARY_PATH environment variable, adding the library directory to the beginning of the existing path
export LD_LIBRARY_PATH=$LIB_DIR:$LD_LIBRARY_PATH

# Run your Java application
java -jar YourApp.jar
# Or using classpath
# java -cp "YourClasspath" YourMainClass
```

**Method 2: Using the JVM Parameter `-Djava.library.path`**

This method directly tells the JVM where to look for library files.

```sh
# Determine the directory containing your .so files, assuming ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# Run the Java application, specifying the library path via the -D parameter
java -Djava.library.path=$LIB_DIR -jar YourApp.jar
# Or using classpath
# java -Djava.library.path=$LIB_DIR -cp "YourClasspath" YourMainClass
```

> **Note**:
>
> - Method 1 (`LD_LIBRARY_PATH`) is recommended as it handles dependencies between libraries better. If only `-Djava.library.path` is used, loading might fail sometimes because a library cannot find its dependencies.
> - Ensure `$LIB_DIR` points to the **exact directory** containing files like `libagora_rtc_sdk.so`.
> - You can include the command to set the environment variable in a startup script for automatic configuration when running the application.

Refer to the following script example, which combines both methods and sets the classpath:

```sh
#!/bin/bash
# Get the absolute path of the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Determine the .so library file path (assuming under libs/native/linux/x86_64 in the script directory)
LIB_PATH="$SCRIPT_DIR/libs/native/linux/x86_64"
# SDK JAR path (assuming under libs in the script directory)
SDK_JAR="$SCRIPT_DIR/libs/agora-sdk.jar"
# Your application's main class
MAIN_CLASS="YourMainClass"
# Your application's other dependency classpath (if any)
APP_CP="YourOtherClasspath"

# Set the library path environment variable
export LD_LIBRARY_PATH=$LIB_PATH:$LD_LIBRARY_PATH

# Combine the classpath
CLASSPATH=".:$SDK_JAR:$APP_CP" # '.' represents the current directory

# Execute the Java program
# Use both LD_LIBRARY_PATH and -Djava.library.path for compatibility
java -Djava.library.path=$LIB_PATH -cp "$CLASSPATH" $MAIN_CLASS
```

## Quick Start

### Official Example Documentation

Refer to the [Official Example Documentation](https://docs.agora.io/en/rtc-server-sdk/java/get-started/run-example/)

### Enable Service

Refer to [Official Service Activation Guide](https://docs.agora.io/en/rtc-server-sdk/java/get-started/enable-service/)

### Run Examples-Mvn Project

**Examples-Mvn** is a Maven example project built on the Spring Boot framework, providing a complete RESTful API service to demonstrate various features of the Agora Linux Server Java SDK.

This project has integrated C++ code compilation functionality, which can automatically compile and generate required .so library files during the Maven build process.

#### Environment Preparation

1. **Install Maven Build Tool**

   Refer to [Maven Installation Guide](https://maven.apache.org/install.html)

   ```bash
   sudo apt-get install maven -y
   sudo apt-get install lsof -y
   ```

2. **C++ Compilation Environment (if native library compilation is needed)**

   Install basic compilation tools:
   ```bash
   sudo apt-get update
   sudo apt-get install build-essential pkg-config gcc g++
   ```

3. **Install C++ Runtime Library**

   The native libraries of the SDK depend on the `libc++` runtime. Please install it to avoid link errors:
   ```bash
   sudo apt-get install libc++1
   ```

4. **FFmpeg Dependencies (if FFmpeg features need to be compiled)**

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev libswresample-dev
   ```

5. **Ensure JAVA_HOME is set correctly**

   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
   ```

#### Project Configuration

1. Enter the `Examples-Mvn` directory:

   ```bash
   cd Examples-Mvn
   ```

2. Create a `.keys` file and add:

   ```
   APP_ID=your_app_id
   TOKEN=your_token
   ```

   _If certificates are not enabled, the TOKEN value can be empty, e.g.:_

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

3. **Runtime Configuration (run_config)**

   The `run_config` file is used to configure various runtime options, located at `Examples-Mvn/run_config`. You can modify the following configurations as needed:

   | Configuration | Type    | Default | Description                                                   |
   | ------------- | ------- | ------- | ------------------------------------------------------------- |
   | enable_asan   | boolean | false   | Whether to enable AddressSanitizer for memory error detection |

   **Configuration Example:**

   ```bash
   # Enable memory checking (debug mode)
   enable_asan=true
   ```

   > **Note**:
   > - The project needs to be recompiled after modifying the configuration.

4. **Configure Java SDK**

    This section describes how to configure the Linux Java SDK dependency for your Maven project.

    **Step 1: Configure JAR Dependency**

    You have two ways to configure the project's JAR dependency:

    **Method 1: Using Maven Central Repository (Recommended)**
    
    If your project can directly fetch dependencies from the Maven Central Repository, ensure that the correct version number is configured in `pom.xml`.
    ```xml
    <dependency>
        <groupId>io.agora.rtc</groupId>
        <artifactId>linux-java-sdk</artifactId>
        <version>4.4.32.201</version>  <!-- Ensure the version number is consistent with the one you need to use -->
    </dependency>
    ```

    **Method 2: Using a Local SDK Package**
    
    If you need to use a local SDK package (e.g., a customized or internal version), follow these steps:

    1.  **Place SDK Artifacts**: Place the downloaded SDK JAR package (e.g., `agora-sdk.jar`) and the corresponding Javadoc package (`agora-sdk-javadoc.jar`) into the `Examples-Mvn/libs/` directory.

    2.  **Install to Local Maven Repository**: In the `linux_server_java` directory, execute the following script. This script will install the JAR files from the `libs` directory as Maven artifacts into your local repository (usually located at `~/.m2/repository`).
        ```bash
        ./build_install_local_maven.sh
        ```
    3.  **Update Project Dependencies**: In your project's `pom.xml` (e.g., `Examples-Mvn`), update the version of the `linux-java-sdk` dependency to match the version you installed locally.


    **Step 2: Configure Native Libraries (`.so`)**
    
    To ensure the Java program can successfully load the native libraries (`.so` files) at runtime, you need to place them in the specified path.

    1.  **Enter the `libs` directory**:
        ```bash
        cd linux_server_java/Examples-Mvn/libs/
        ```

    2.  **Extract Native Libraries from JAR**:
        ```bash
        # -x: extract, -v: verbose, -f: file
        jar -xvf agora-sdk.jar native/
        ```
        This command extracts the `native` directory from `agora-sdk.jar`, which contains the native libraries for all platforms.

    3.  **Verify Directory Structure**:
        After extraction, the `libs` directory structure should be as follows, ensuring the `.so` files are located under the `native/linux/x86_64/` path:
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
    4.  **Return to the project root directory**:
        ```bash
        cd ../../..
        ```

#### Build Process

Execute the build script:

```bash
# Standard Maven build (without compiling native code)
./build.sh

# Compile and start service
./build.sh start

# Compile all native libraries
./build.sh -native

# Compile all native libraries and start service
./build.sh -native start

# Compile only FFmpeg-related libraries
./build.sh -ffmpegUtils

# Compile only Media-related libraries
./build.sh -mediaUtils
```

**Build Options:**
- By default, only compiles the Java project without compiling C++ code
- Use `-native` option to compile all native libraries (FFmpeg + Media)
- Use `-ffmpegUtils` option to compile only FFmpeg-related libraries (for MP4 processing)
- Use `-mediaUtils` option to compile only Media-related libraries (for encoded audio/video processing)
- Use `start` option to automatically start the service after compilation

**Using Maven Commands:**

You can also use Maven commands directly:

```bash
# Compile all native libraries
mvn clean package -Dbuild.native=true

# Compile only FFmpeg libraries
mvn clean package -Dbuild.ffmpeg=true

# Compile only Media libraries
mvn clean package -Dbuild.media=true
```

#### Running Examples

After starting the service, use a browser or Postman to access the following interface addresses to test various features:

**Basic Feature Test Interface:**
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

**Configuration File Interface:**
```
http://localhost:18080/api/server/start?configFileName=pcm_send.json
```

> **Note**: Replace `localhost:18080` with your actual server address and port.

#### Test Cases

- Send PCM Audio

  Refer to [SendPcmFileTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendPcmFileTest.java), implement looping PCM file sending

  Refer to [SendPcmRealTimeTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendPcmRealTimeTest.java), implement streaming PCM data sending

- Send YUV Video

  Refer to [SendYuvTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendYuvTest.java), implement streaming YUV data sending

- Send H264 Video

  Refer to [SendH264Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendH264Test.java), implement streaming H264 data sending

- Send Opus Audio

  Refer to [SendOpusTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendOpusTest.java), implement streaming Opus data sending

- Send MP4 Audio/Video

  Refer to [SendMp4Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendMp4Test.java), implement MP4 file sending

- Receive PCM Audio

  Refer to [ReceiverPcmVadTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmVadTest.java), implement receiving PCM data with VAD data

  Refer to [ReceiverPcmDirectSendTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmDirectSendTest.java), implement receiving PCM data and directly sending back

- Receive PCM&H264 Audio/Video

  Refer to [ReceiverPcmH264Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmH264Test.java), implement receiving PCM & H264 data

- Receive PCM&YUV Audio/Video

  Refer to [ReceiverPcmYuvTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmYuvTest.java), implement receiving PCM & YUV data

- Send/Receive Stream Messages

  Refer to [SendReceiverStreamMessageTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/SendReceiverStreamMessageTest.java), implement sending and receiving stream messages

- APM Features

  Refer to [ExternalAudioProcessorTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ExternalAudioProcessorTest.java), implement APM features

## API and Feature Modules

### API Documentation Reference

For complete API documentation, refer to the following resources:

- [API-reference.md](./API-reference.md) file (For reference only, based on the Chinese version)
- Official Documentation [Agora Java Server SDK API Reference](https://docs.agora.io/en/rtc-server-sdk/java/reference/api-overview/) (Official documentation takes precedence)

### APM Features

The APM (Audio Processing Module) provides server-side audio processing capabilities, including Acoustic Echo Cancellation (AEC), Automatic Noise Suppression (ANS), Automatic Gain Control (AGC), and Background Human Voice Suppression (BGHVS).

> **Note**: Typically, features like AEC/ANS/AGC are already implemented in the client-side SDK, so server-side processing is not required unless there are specific business needs (e.g., processing raw audio streams from non-Agora SDK clients).

If you determine that you need to enable server-side APM features, please contact Agora Technical Support for detailed guidance and configuration instructions.

#### Usage Modes

APM features support two usage modes:

| Mode        | Description                                                                                   | Requires Joining Channel |
| ----------- | --------------------------------------------------------------------------------------------- | ------------------------ |
| Local Mode  | Local audio processing, no channel join required, push PCM data directly for processing       | No                       |
| Remote Mode | Remote audio processing, requires joining a channel, processes audio streams from the channel | Yes                      |

#### Local Mode

Local mode is suitable for scenarios that don't require joining a channel. Push local PCM audio data directly for VAD and/or 3A+BGHVS processing.

##### Usage Scenarios

| Scenario         | vadConfig | apmConfig | Description                                   |
| ---------------- | --------- | --------- | --------------------------------------------- |
| VAD Only         | not null  | null      | Voice activity detection only                 |
| VAD + 3A + BGHVS | not null  | not null  | Voice activity detection and audio processing |

##### Core Classes

###### AgoraExternalAudioProcessor Class

External audio processor for 3A (AEC, ANS, AGC) and VAD processing of audio data.

**Creation Method**

```java
AgoraExternalAudioProcessor audioProcessor = service.createExternalAudioProcessor();
```

**Initialization Method**

```java
public int initialize(AgoraApmConfig apmConfig, int outputSampleRate, int outputChannels,
        AgoraAudioVadConfigV2 vadConfig, IExternalAudioProcessorObserver observer)
```

- **Parameters**
  - `apmConfig`: `AgoraApmConfig` type, APM configuration. Set to `null` to disable 3A+BGHVS processing
  - `outputSampleRate`: Output sample rate
  - `outputChannels`: Output channel count
  - `vadConfig`: `AgoraAudioVadConfigV2` type, VAD configuration. Set to `null` to disable VAD
  - `observer`: `IExternalAudioProcessorObserver` type, audio frame callback observer
- **Returns**
  - 0 for success, negative value for failure

**Push Audio Data Method**

```java
public int pushAudioPcmData(byte[] data, int sampleRate, int channels, long presentationMs)
```

- **Parameters**
  - `data`: PCM audio data (16-bit)
  - `sampleRate`: Sample rate
  - `channels`: Channel count
  - `presentationMs`: Audio frame PTS (milliseconds)
- **Returns**
  - 0 for success, negative value for failure

**Destroy Method**

```java
public void destroy()
```

###### IExternalAudioProcessorObserver Interface

External audio processor observer interface for receiving processed audio frames.

```java
public interface IExternalAudioProcessorObserver {
    void onAudioFrame(AgoraExternalAudioProcessor audioProcessor, AudioFrame audioFrame, VadProcessResult vadProcessResult);
}
```

- **Parameters**
  - `audioProcessor`: The audio processor instance that generated this callback, useful when multiple processors share the same observer
  - `audioFrame`: Processed audio frame
  - `vadProcessResult`: VAD processing result, containing VAD state and output frame

##### Scenario 1: VAD Only

Voice activity detection only, without enabling 3A+BGHVS processing.

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
        // 1. Initialize AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);
        service.initialize(config);

        // 2. Create external audio processor
        AgoraExternalAudioProcessor audioProcessor = service.createExternalAudioProcessor();

        // 3. Configure VAD (see VAD Configuration Parameters)
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // Use default values, adjust as needed

        // 4. Initialize processor (apmConfig set to null, VAD only)
        int ret = audioProcessor.initialize(
            null,  // apmConfig is null, 3A+BGHVS disabled
            16000, // Output sample rate
            1,     // Output channel count
            vadConfig,
            new IExternalAudioProcessorObserver() {
                @Override
                public void onAudioFrame(AgoraExternalAudioProcessor audioProcessor, AudioFrame audioFrame, VadProcessResult vadProcessResult) {
                    // audioProcessor can be used to identify which processor the callback is from (when sharing observer)
                    // Handle callback
                    if (vadProcessResult != null) {
                        System.out.println("VAD State: " + vadProcessResult.getState());
                        // vadProcessResult.getOutFrame() contains VAD-processed audio data
                    }
                }
            }
        );

        if (ret != 0) {
            System.err.println("Initialize failed: " + ret);
            return;
        }

        // 5. Push PCM audio data (10ms per frame, 16kHz mono = 320 bytes)
        byte[] pcmData = new byte[320]; // 16000 * 1 * 2 / 100 = 320 bytes per 10ms
        // ... Fill PCM data ...
        long presentationMs = 0;
        audioProcessor.pushAudioPcmData(pcmData, 16000, 1, presentationMs);

        // 6. Destroy resources
        audioProcessor.destroy();
        service.destroy();
    }
}
```

##### Scenario 2: VAD + 3A + BGHVS

Voice activity detection and 3A+BGHVS audio processing simultaneously.

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
        // 1. Initialize AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);
        service.initialize(config);

        // 2. Create external audio processor
        AgoraExternalAudioProcessor audioProcessor = service.createExternalAudioProcessor();

        // 3. Configure APM (3A + BGHVS)
        AgoraApmConfig apmConfig = new AgoraApmConfig();
        // AEC, ANS, AGC, BGHVS are enabled by default, adjust as needed
        // apmConfig.getAiAecConfig().setEnabled(true);   // Echo cancellation
        // apmConfig.getAiNsConfig().setNsEnabled(true);  // Noise suppression
        // apmConfig.getAgcConfig().setEnabled(true);     // Automatic gain control
        // apmConfig.getBghvsConfig().setEnabled(true);   // Background human voice suppression
        // apmConfig.setEnableDump(false);                // Enable dump for debugging

        // 4. Configure VAD (see VAD Configuration Parameters)
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // Use default values, adjust as needed

        // 5. Initialize processor (enable both apmConfig and vadConfig)
        int ret = audioProcessor.initialize(
            apmConfig,  // Enable 3A+BGHVS
            16000,      // Output sample rate
            1,          // Output channel count
            vadConfig,  // Enable VAD
            new IExternalAudioProcessorObserver() {
                @Override
                public void onAudioFrame(AgoraExternalAudioProcessor audioProcessor, AudioFrame audioFrame, VadProcessResult vadProcessResult) {
                    // audioProcessor can be used to identify which processor the callback is from (when sharing observer)
                    // audioFrame contains 3A+BGHVS processed audio data
                    if (audioFrame != null) {
                        byte[] processedData = io.agora.rtc.utils.Utils.getBytes(audioFrame.getBuffer());
                        // ... Use processed audio data ...
                    }
                    
                    // vadProcessResult contains VAD processing result
                    if (vadProcessResult != null) {
                        System.out.println("VAD State: " + vadProcessResult.getState());
                        // vadProcessResult.getOutFrame() contains VAD-processed audio data
                    }
                }
            }
        );

        if (ret != 0) {
            System.err.println("Initialize failed: " + ret);
            return;
        }

        // 6. Push PCM audio data (10ms per frame, 16kHz mono = 320 bytes)
        byte[] pcmData = new byte[320];
        // ... Fill PCM data ...
        long presentationMs = 0;
        audioProcessor.pushAudioPcmData(pcmData, 16000, 1, presentationMs);

        // 7. Destroy resources
        audioProcessor.destroy();
        service.destroy();
    }
}
```

##### Complete Example

For complete example code, refer to: `Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ExternalAudioProcessorTest.java`

#### Remote Mode

Remote mode is suitable for scenarios requiring channel join, receiving and processing remote audio streams from the channel with VAD and/or 3A+BGHVS processing.

##### Usage Scenarios

| Scenario         | AgoraServiceConfig.apmConfig | registerAudioFrameObserver vadConfig | Description                                   |
| ---------------- | ---------------------------- | ------------------------------------ | --------------------------------------------- |
| VAD Only         | null or not set              | not null                             | Voice activity detection only                 |
| VAD + 3A + BGHVS | not null                     | not null                             | Voice activity detection and audio processing |

##### Core Configuration

###### AgoraServiceConfig Configuration

Configure APM through `AgoraServiceConfig` when initializing `AgoraService`:

```java
AgoraServiceConfig config = new AgoraServiceConfig();
config.setApmMode(Constants.ApmMode.ENABLE);  // Enable APM mode

// Configure 3A+BGHVS (optional, set to null to disable)
AgoraApmConfig apmConfig = new AgoraApmConfig();
config.setApmConfig(apmConfig);  // Set to null to disable 3A+BGHVS
```

###### Register Audio Frame Observer

Register audio frame observer and configure VAD through the `registerAudioFrameObserver` method:

```java
conn.registerAudioFrameObserver(audioFrameObserver, true, new AgoraAudioVadConfigV2());
```

- **Parameters**
  - `observer`: `IAudioFrameObserver` type, audio frame callback observer
  - `enableVad`: Whether to enable VAD
  - `vadConfig`: `AgoraAudioVadConfigV2` type, VAD configuration

###### IAudioFrameObserver Interface

Audio frame observer interface for receiving processed remote audio frames.

```java
public interface IAudioFrameObserver {
    int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
            String channelId, String userId, AudioFrame frame, VadProcessResult vadResult);
}
```

- **Parameters**
  - `agoraLocalUser`: Local user object
  - `channelId`: Channel ID
  - `userId`: Remote user ID
  - `frame`: Processed audio frame (3A+BGHVS processed if enabled)
  - `vadResult`: VAD processing result, containing VAD state and output frame

##### Scenario 1: VAD Only

Voice activity detection only, without enabling 3A+BGHVS processing.

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
        // 1. Initialize AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);
        // Don't set apmConfig, 3A+BGHVS disabled
        // config.setApmConfig(null);
        service.initialize(config);

        // 2. Create connection
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, null);

        // 3. Subscribe audio
        conn.getLocalUser().subscribeAllAudio();

        // 4. Set audio parameters
        conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(1, 16000);

        // 5. Register audio frame observer (enable VAD)
        IAudioFrameObserver audioFrameObserver = new IAudioFrameObserver() {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
                    String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
                if (frame == null) {
                    return 0;
                }
                
                // Process audio frame
                byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(frame.getBuffer());
                
                // Process VAD result
                if (vadResult != null) {
                    System.out.println("VAD State: " + vadResult.getState());
                    if (vadResult.getState() == Constants.VadState.START_SPEAKING
                            || vadResult.getState() == Constants.VadState.SPEAKING
                            || vadResult.getState() == Constants.VadState.STOP_SPEAKING) {
                        byte[] vadData = vadResult.getOutFrame();
                        // ... Use VAD-processed audio data ...
                    }
                }
                return 1;
            }
        };

        // Configure VAD (see VAD Configuration Parameters)
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // Use default values, adjust as needed
        conn.registerAudioFrameObserver(audioFrameObserver, true, vadConfig);

        // 6. Join channel
        conn.connect("your_token", "channel_id", "user_id");

        // ... Business logic ...

        // 7. Leave channel and destroy resources
        conn.disconnect();
        conn.destroy();
        service.destroy();
    }
}
```

##### Scenario 2: VAD + 3A + BGHVS

Voice activity detection and 3A+BGHVS audio processing simultaneously.

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
        // 1. Initialize AgoraService
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId("your_app_id");
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        config.setApmMode(Constants.ApmMode.ENABLE);

        // Configure APM (3A + BGHVS)
        AgoraApmConfig apmConfig = new AgoraApmConfig();
        // AEC, ANS, AGC, BGHVS are enabled by default, adjust as needed
        config.setApmConfig(apmConfig);  // Enable 3A+BGHVS

        service.initialize(config);

        // 2. Create connection
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, null);

        // 3. Subscribe audio
        conn.getLocalUser().subscribeAllAudio();

        // 4. Set audio parameters
        conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(1, 16000);

        // 5. Register audio frame observer (enable VAD)
        IAudioFrameObserver audioFrameObserver = new IAudioFrameObserver() {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
                    String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
                if (frame == null) {
                    return 0;
                }
                
                // frame contains 3A+BGHVS processed audio data
                byte[] processedData = io.agora.rtc.utils.Utils.getBytes(frame.getBuffer());
                // ... Use processed audio data ...
                
                // Process VAD result
                if (vadResult != null) {
                    System.out.println("VAD State: " + vadResult.getState());
                    if (vadResult.getState() == Constants.VadState.START_SPEAKING
                            || vadResult.getState() == Constants.VadState.SPEAKING
                            || vadResult.getState() == Constants.VadState.STOP_SPEAKING) {
                        byte[] vadData = vadResult.getOutFrame();
                        // ... Use VAD-processed audio data ...
                    }
                }
                return 1;
            }
        };

        // Configure VAD (see VAD Configuration Parameters)
        AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();
        // Use default values, adjust as needed
        conn.registerAudioFrameObserver(audioFrameObserver, true, vadConfig);

        // 6. Join channel
        conn.connect("your_token", "channel_id", "user_id");

        // ... Business logic ...

        // 7. Leave channel and destroy resources
        conn.disconnect();
        conn.destroy();
        service.destroy();
    }
}
```

##### Complete Example

For complete example code, refer to: `Examples-Mvn/src/main/java/io/agora/rtc/example/basic/ReceiverPcmVadTest.java`

#### VAD Configuration Parameters

VAD configuration uses the `AgoraAudioVadConfigV2` class.

##### AgoraAudioVadConfigV2 Properties

| Property Name              | Type    | Description                                                         | Default | Range                  |
| -------------------------- | ------- | ------------------------------------------------------------------- | ------- | ---------------------- |
| preStartRecognizeCount     | int     | Number of audio frames saved before entering speaking state         | 16      | [0, Integer.MAX_VALUE] |
| startRecognizeCount        | int     | Number of audio frames to confirm speaking state                    | 30      | [1, Integer.MAX_VALUE] |
| stopRecognizeCount         | int     | Number of audio frames to confirm stop speaking state               | 65      | [1, Integer.MAX_VALUE] |
| activePercent              | float   | Percentage of active frames required in startRecognizeCount frames  | 0.7     | [0.0, 1.0]             |
| inactivePercent            | float   | Percentage of inactive frames required in stopRecognizeCount frames | 0.5     | [0.0, 1.0]             |
| startVoiceProb             | int     | Probability threshold to start voice detection                      | 70      | [0, 100]               |
| stopVoiceProb              | int     | Probability threshold to stop voice detection                       | 70      | [0, 100]               |
| startRmsThreshold          | int     | RMS threshold (dB) to start voice detection                         | -70     | [-100, 0]              |
| stopRmsThreshold           | int     | RMS threshold (dB) to stop voice detection                          | -70     | [-100, 0]              |
| enableAdaptiveRmsThreshold | boolean | Enable adaptive RMS threshold                                       | true    | true/false             |
| adaptiveRmsThresholdFactor | float   | Adaptive RMS threshold factor                                       | 0.67    | [0.0, 1.0]             |

##### Parameter Description

**Window Size Parameters**:
- `preStartRecognizeCount`: Pre-start buffer frames to preserve voice beginning (16 frames = 160ms)
- `startRecognizeCount`: Start detection window size to determine speech start (30 frames = 300ms)
- `stopRecognizeCount`: Stop detection window size to determine speech end (65 frames = 650ms)

**Percentage Thresholds**:
- `activePercent`: Start active frame ratio threshold (default 0.7 = 70%), higher value for stricter start
- `inactivePercent`: Stop inactive frame ratio threshold (default 0.5 = 50%), higher value for quicker stop

**Voice Probability Thresholds**:
- `startVoiceProb`: Start voice probability threshold (0-100), lower value for more sensitivity and earlier detection
- `stopVoiceProb`: Stop voice probability threshold (0-100), higher value for earlier stop detection

**RMS Energy Thresholds**:
- `startRmsThreshold`: Start RMS threshold (dB), higher value for more sensitivity to voice
  - Quiet environment: -70 dB (default)
  - Noisy environment: -50 to -40 dB
  - High noise environment: -40 to -30 dB
- `stopRmsThreshold`: Stop RMS threshold (dB), typically same as startRmsThreshold

**Adaptive Thresholds**:
- `enableAdaptiveRmsThreshold`: When enabled, automatically adjusts RMS threshold based on voice statistics for better environment adaptation
- `adaptiveRmsThresholdFactor`: Adaptive factor (default 0.67 = 2/3), lower value for lower threshold and more sensitivity

##### VadProcessResult

Stores the result of VAD processing.

```java
public VadProcessResult(byte[] result, Constants.VadState state)
```

- **Parameters**
  - `result`: `byte[]` type, the processed audio data.
  - `state`: `Constants.VadState` type, the current VAD state.

**VAD State Description**:
- `UNKNOWN`: Unknown state
- `NOT_SPEAKING`: Not speaking
- `START_SPEAKING`: Start speaking
- `SPEAKING`: Speaking
- `STOP_SPEAKING`: Stop speaking

## Changelog

### v4.4.32.201 (2025-12-18)

- **API Changes**
  - **AgoraServiceConfig**: 
    - Changed `enableApm` property to `apmMode`, with type changed from `boolean` to `Constants.ApmMode` enum.
    - Added `Constants.ApmMode` enum, supporting `DISABLE` and `ENABLE` modes.
    - Default value changed from `true` to `Constants.ApmMode.DISABLE`.
  - **AgoraExternalAudioProcessor**: 
    - Added external audio processor class, supporting audio processing in local APM mode.
    - Provides capabilities for PCM audio data pushing and receiving processed audio.
  - **IExternalAudioProcessorObserver**: 
    - Added external audio processor observer interface for receiving processed audio frame callbacks.

- **Improvements & Optimizations**
  - Fixed memory leak issues, optimized model class destruction process.

### v4.4.32.200 (2025-11-14)

- **API Changes**
  - **AgoraServiceConfig**: Added `enableApm` and `apmConfig` properties to support configuring the APM (Audio Processing Module) module.

- **Improvements & Optimizations**
  - Optimized the default configuration parameters for VAD V2, improving the accuracy of voice activity detection.
  - Fixed the accuracy issue of PTS values in Audio and Video callback parameters.

### v4.4.32.101 (2025-09-01)

- **API Changes**
  - **AudioFrame**: Added `presentationMs` field and getter/setter to carry audio frame PTS (ms).
  - **EncodedVideoFrameInfo**: Added `presentationMs` field and constructor parameter; can be used to pass video frame PTS (ms).
  - **EncodedAudioFrameInfo**: Added `captureTimeMs` field and constructor parameter; records capture timestamp (ms).
  - **AgoraRtcConn**: Added overload `pushAudioPcmData(byte[] data, int sampleRate, int channels, long presentationMs)`; the original `pushAudioPcmData(byte[] data, int sampleRate, int channels)` remains (equivalent to `presentationMs=0`).

- **Improvements & Optimizations**
  - Fixed a potential exception in `IAudioFrameObserver` callback under extreme scenarios, improving stability.

### v4.4.32.100（2025-07-22）

- **API Changes**
  - This version supports AIQoS, and the API has changed, please refer to [AIQoS Upgrade Guide](AIQoS_Upgrade_Guide.md)

### v4.4.32.1 (2025-06-12)

- **API Changes**
  - Optimized the `onStreamMessage` callback parameters in the `ILocalUserObserver` interface. The original method `onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data, long length)` has been changed to `onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, byte[] data)` to improve flexibility and efficiency in message handling.

- **Improvements & Optimizations**
  - Fixed an issue in the `setLogFileSize` method of `AgoraServiceConfig` where the unit was incorrectly applied as bytes when set in KB. The log file size is now correctly set in KB.

### v4.4.32 (2025-05-27)

- **API Changes**
  - `AgoraService`: Added `getSdkVersion` method to obtain the SDK version.
  - `AgoraAudioEncodedFrameSender`: Removed `send(byte[] payloadData, int payloadSize, EncodedAudioFrameInfo info)` method, replaced with `sendEncodedAudioFrame(byte[] payloadData, EncodedAudioFrameInfo info)`.
  - `AgoraAudioPcmDataSender`: The method `send(byte[] audioData, int captureTimestamp, int samplesPerChannel, int bytesPerSample, int numberOfChannels, int sampleRate)` is now deprecated, replaced with `sendAudioPcmData(AudioFrame audioFrame)`.
  - `AgoraVideoEncodedImageSender`: Removed `send(byte[] imageBuffer, int length, EncodedVideoFrameInfo info)` method, replaced with `sendEncodedVideoImage(byte[] imageBuffer, EncodedVideoFrameInfo info)`.
  - `AgoraVideoFrameSender`: Removed `send(ExternalVideoFrame frame)` method, replaced with `sendVideoFrame(ExternalVideoFrame frame)`.

- **Improvements & Optimizations**
  - Fixed a potential crash issue caused by the `destroy` method.

### v4.4.31.4 (2025-03-21)

- **Improvements & Optimizations**
  - Fixed potential crashes caused by exceptions in multi-threaded environments.
  - Improved error handling processes, enhancing recovery capabilities in exceptional circumstances.

### v4.4.31.3 (2025-02-26)

- **Improvements & Optimizations**
  - Fixed exception handling issues potentially caused by memory reuse.

### v4.4.31.2 (2025-02-19)

- **API Changes**

- Added `sendStreamMessage(int streamId, byte[] messageData)` method, deprecated `sendStreamMessage(int streamId, String message, int length)` method.

- **Improvements & Optimizations**
  - Optimized code handling, improving system robustness.

### v4.4.31.1 (2025-01-06)

- **Improvements & Optimizations**
  - Optimized VAD function configuration; VAD is now enabled by default, manual configuration is not required.

### v4.4.31 (2024-12-23)

- **API Changes**
  - Added `DomainLimit` configuration option in `AgoraServiceConfig` for domain limit management.
  - Added `VadDumpUtils` utility class, supporting export of debug data from the VAD process.
  - Added `AudioConsumerUtils` class, providing an optimized PCM data transmission mechanism.
  - Modified `registerAudioFrameObserver` method in `AgoraLocalUser` to support configuration of `AgoraAudioVadConfigV2` parameters.
  - Added `vadResult` parameter to the `onPlaybackAudioFrameBeforeMixing` callback in `IAudioFrameObserver`.
  - Added `sendAudioMetaData` method in `AgoraLocalUser` class to support sending audio metadata.
  - Added `onAudioMetaDataReceived` callback in `ILocalUserObserver` class for receiving audio metadata.
  - Added `ColorSpace` property in `ExternalVideoFrame` class to support custom color space settings.

- **Improvements & Optimizations**
  - Optimized code logic architecture, significantly improving memory usage efficiency.
  - Fixed multiple memory leak issues, enhancing system stability.
  - Strengthened memory access security mechanisms, effectively preventing memory corruption issues.

### v4.4.30.2 (2024-11-20)

- **API Changes**
  - Enhanced `processFrame` handling in AgoraAudioVadV2, adding `START_SPEAKING` and `STOP_SPEAKING` state callbacks.
  - Improved parameter types for encoded frame callbacks: `onEncodedAudioFrameReceived`, `onEncodedVideoImageReceived`, `onEncodedVideoFrame` now use `ByteBuffer` instead of `Byte` arrays.

- **Improvements & Optimizations**
  - VAD plugin startup optimization: `enableExtension` is now handled internally by the SDK; applications no longer need to call this method manually.
  - Fixed handling issues with `alphaBuffer` and `metadataBuffer` in `VideoFrame`.

### v4.4.30.1 (2024-11-12)

- **API Changes**
  - Added AgoraAudioVad2 related `Vad2` interface, removed AgoraAudioVad related `Vad` interface.
  - Added callback interface `IAudioEncodedFrameObserver` for receiving encoded audio.

- **Improvements & Optimizations**
  - Fixed crash issues related to `LocalAudioDetailedStats` callbacks.
  - Modified parameter types for the `onAudioVolumeIndication` callback.

### v4.4.30 (2024-10-24)
- **API Changes**
  - For detailed changelog, please refer to the [Release Notes](https://docs.agora.io/en/rtc-server-sdk/java/overview/release-notes/)

## Other References

- Refer to the official website for details (<https://docs.agora.io/en/rtc-server-sdk/java/landing-page/>)

- Official API documentation [Agora Server Java SDK API Reference](https://docs.agora.io/en/rtc-server-sdk/java/reference/api-overview/)
