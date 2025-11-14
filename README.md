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
  - [API Reference](#api-reference)
    - [API Documentation Reference](#api-documentation-reference)
    - [VAD Module](#vad-module)
      - [VadV1 Module (Only supported by Gateway SDK)](#vadv1-module-only-supported-by-gateway-sdk)
        - [Introduction](#introduction-1)
        - [Classes and Methods](#classes-and-methods)
          - [AgoraAudioVad Class](#agoraaudiovad-class)
          - [AgoraAudioVadConfig Class](#agoraaudiovadconfig-class)
        - [Usage Example](#usage-example)
      - [VadV2 Module](#vadv2-module)
        - [Introduction](#introduction-2)
        - [Classes and Methods](#classes-and-methods-1)
          - [AgoraAudioVadV2 Class](#agoraaudiovadv2-class)
          - [AgoraAudioVadConfigV2 Properties](#agoraaudiovadconfigv2-properties)
          - [Parameter Description](#parameter-description)
          - [Methods](#methods)
        - [VadProcessResult](#vadprocessresult)
          - [Constructor](#constructor)
        - [Usage Example](#usage-example-1)
    - [Audio 3A Module (Only supported by Gateway SDK)](#audio-3a-module-only-supported-by-gateway-sdk)
      - [Introduction](#introduction-3)
      - [Classes and Methods](#classes-and-methods-2)
        - [AgoraAudioProcessor Class](#agoraaudioprocessor-class)
          - [Constructor](#constructor-1)
          - [Methods](#methods-1)
        - [AgoraAudioProcessorConfig Class](#agoraaudioprocessorconfig-class)
          - [Methods](#methods-2)
          - [Example](#example)
        - [IAgoraAudioProcessorEventHandler Interface](#iagoraaudioprocessoreventhandler-interface)
          - [Methods](#methods-3)
        - [io.agora.rtc.audio3a.AgoraAudioFrame Class](#ioagorartcaudio3aagoraaudioframe-class)
          - [Key Properties](#key-properties)
          - [Main Methods (Setters/Getters)](#main-methods-settersgetters)
      - [Usage Example](#usage-example-2)
  - [Changelog](#changelog)
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

The Agora Linux Server Java SDK (v4.4.32.200) provides powerful real-time audio and video communication capabilities that can be seamlessly integrated into Linux server-side Java applications. With this SDK, your server can join Agora channels as a data source or processing node, accessing and processing audio and video streams in real-time to implement various business-related advanced features.

The Agora Linux Gateway SDK has not been released yet, and related features are currently not supported.

> Note: If you are upgrading from a version earlier than v4.4.32.100 to v4.4.32.100 or later, please refer to the [AIQoS Upgrade Guide](./AIQoS_Upgrade_Guide.md) for required API and integration changes.

## Development Environment Requirements

### Hardware Environment

- **Operating System**: Ubuntu 18.04+ or CentOS 7.0+
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
    <version>4.4.32.200</version>
</dependency>
```

### CDN Download

[Agora-Linux-Java-SDK-v4.4.32.200-x86_64-964478-6b09067690-20251114_115603](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.32.200-x86_64-964478-6b09067690-20251114_115603.zip)

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
    <version>4.4.32.200</version>
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
  -Dversion=4.4.32.200 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

Method 2: Install both SDK JAR and JavaDoc JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32.200 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -Djavadoc=sdk/agora-sdk-javadoc.jar
```

After installation, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32.200</version>
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
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-java-sdk/4.4.32.200/linux-java-sdk-4.4.32.200.jar
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
                ├── libaosl.so
                └── libbinding.so
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

   If Gateway SDK is supported, create a `.keys_gateway` file and add:

   ```
   APP_ID=your_app_id
   LICENSE=your_license
   ```

3. **Runtime Configuration (run_config)**

   The `run_config` file is used to configure various runtime options, located at `Examples-Mvn/run_config`. You can modify the following configurations as needed:

   | Configuration  | Type    | Default | Description                                                                          |
   | -------------- | ------- | ------- | ------------------------------------------------------------------------------------ |
   | enable_asan    | boolean | false   | Whether to enable AddressSanitizer for memory error detection                        |
   | enable_gateway | boolean | false   | Whether to enable Gateway SDK mode, which allows access to VAD and Audio 3A features |

   **Configuration Example:**

   ```bash
   # Enable Gateway SDK functionality
   enable_gateway=true
  
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
        <version>4.4.32.200</version>  <!-- Ensure the version number is consistent with the one you need to use -->
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
                    └── ... (other .so files)
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

**Gateway SDK Exclusive Features:**
```
http://localhost:18080/api/server/basic?taskName=VadV1Test
http://localhost:18080/api/server/basic?taskName=Audio3aTest
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

- VadV1 Module (Only supported by Gateway SDK)

  Refer to [VadV1Test.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/VadV1Test.java.disabled), implement VadV1 module functionality

- Audio 3A Processing (Only supported by Gateway SDK)

  Refer to [Audio3aTest.java](./Examples-Mvn/src/main/java/io/agora/rtc/example/basic/Audio3aTest.java.disabled), implement audio 3A processing functionality

## API Reference

### API Documentation Reference

For complete API documentation, refer to the following resources:

- [API-reference.md](./API-reference.md) file (For reference only, based on the Chinese version)
- Official Documentation [Agora Java Server SDK API Reference](https://docs.agora.io/en/rtc-server-sdk/java/reference/api-overview/) (Official documentation takes precedence)

### VAD Module

#### VadV1 Module (Only supported by Gateway SDK)

##### Introduction

`AgoraAudioVad` is a Voice Activity Detection (VAD) module for processing audio frames. It detects speech activity in an audio stream and processes it based on configuration parameters. This module is the first version of VAD, providing basic voice activity detection functionality.

##### Classes and Methods

###### AgoraAudioVad Class

**Constructor**

```java
public AgoraAudioVad()
```

- **Description**: Constructs an `AgoraAudioVad` instance.

**Methods**

```java
public int initialize(AgoraAudioVadConfig config)
```

- **Description**: Initializes the VAD module. This must be called before using other methods.
- **Parameters**:
  - `config`: `AgoraAudioVadConfig` type, VAD configuration.
- **Returns**: `int`, 0 for success, -1 for failure.

```java
public VadProcessResult processPcmFrame(byte[] frame)
```

- **Description**: Processes PCM audio frames.
- **Parameters**:
  - `frame`: `byte[]` type, PCM audio data.
- **Returns**: `VadProcessResult` type, VAD processing result.

```java
public synchronized void destroy()
```

- **Description**: Destroys the VAD module and releases resources.

###### AgoraAudioVadConfig Class

**Main Properties**

| Property Name          | Type  | Description                                           | Default | Range                  |
| ---------------------- | ----- | ----------------------------------------------------- | ------- | ---------------------- |
| fftSz                  | int   | FFT size, supports only 128, 256, 512, 1024           | 1024    | [128, 256, 512, 1024]  |
| hopSz                  | int   | FFT hop size, used for checking                       | 160     | [1, Integer.MAX_VALUE] |
| anaWindowSz            | int   | FFT window size, used for calculating RMS             | 768     | [1, Integer.MAX_VALUE] |
| voiceProbThr           | float | Voice probability threshold                           | 0.7     | [0.0, 1.0]             |
| rmsThr                 | float | RMS threshold (dB)                                    | -40.0   | [-100.0, 0.0]          |
| jointThr               | float | Joint threshold (dB)                                  | 0.0     | [-100.0, 100.0]        |
| aggressive             | float | Aggressive factor, higher value means more aggressive | 2.0     | [0.0, 10.0]            |
| startRecognizeCount    | int   | Start recognition count                               | 30      | [1, Integer.MAX_VALUE] |
| stopRecognizeCount     | int   | Stop recognition count                                | 48      | [1, Integer.MAX_VALUE] |
| preStartRecognizeCount | int   | Pre-start recognition count                           | 16      | [0, Integer.MAX_VALUE] |
| activePercent          | float | Active percentage                                     | 0.8     | [0.0, 1.0]             |
| inactivePercent        | float | Inactive percentage                                   | 0.2     | [0.0, 1.0]             |

##### Usage Example

Here is a simple example showing how to use `AgoraAudioVad` for audio frame processing:

```java
import io.agora.rtc.AgoraAudioVad;
import io.agora.rtc.AgoraAudioVadConfig;
import io.agora.rtc.VadProcessResult;
import java.io.FileInputStream;

public class VadV1Example {
    public static void main(String[] args) {
        // Create VAD instance
        AgoraAudioVad audioVad = new AgoraAudioVad();
        
        // Create configuration
        AgoraAudioVadConfig config = new AgoraAudioVadConfig();
        // Configure parameters as needed, recommend using default values
        
        // Initialize VAD
        int ret = audioVad.initialize(config);
        if (ret != 0) {
            System.err.println("Failed to initialize VAD: " + ret);
            return;
        }
        
        // Process audio frames
        try {
            // Assume we have PCM audio data
            byte[] pcmData = new byte[320]; // 10ms 16kHz mono PCM16 data
            
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
        
        // Destroy VAD instance
        audioVad.destroy();
    }
}
```

#### VadV2 Module

##### Introduction

`AgoraAudioVadV2` is the second version of the Voice Activity Detection (VAD) module for processing audio frames. It detects speech activity in an audio stream and processes it based on configuration parameters.

##### Classes and Methods

###### AgoraAudioVadV2 Class

**Constructor**

```java
public AgoraAudioVadV2(AgoraAudioVadConfigV2 config)
```

- **Parameters**
  - `config`: `AgoraAudioVadConfigV2` type, VAD configuration.

###### AgoraAudioVadConfigV2 Properties

| Property Name                | Type    | Description                                                         | Default | Range                  |
| ---------------------------- | ------- | ------------------------------------------------------------------- | ------- | ---------------------- |
| preStartRecognizeCount       | int     | Number of audio frames saved before entering speaking state         | 16      | [0, Integer.MAX_VALUE] |
| startRecognizeCount          | int     | Number of audio frames to confirm speaking state                    | 30      | [1, Integer.MAX_VALUE] |
| stopRecognizeCount           | int     | Number of audio frames to confirm stop speaking state               | 65      | [1, Integer.MAX_VALUE] |
| activePercent                | float   | Percentage of active frames required in startRecognizeCount frames  | 0.7     | [0.0, 1.0]             |
| inactivePercent              | float   | Percentage of inactive frames required in stopRecognizeCount frames | 0.5     | [0.0, 1.0]             |
| startVoiceProb               | int     | Probability threshold to start voice detection                      | 70      | [0, 100]               |
| stopVoiceProb                | int     | Probability threshold to stop voice detection                       | 70      | [0, 100]               |
| startRmsThreshold            | int     | RMS threshold (dB) to start voice detection                         | -70     | [-100, 0]              |
| stopRmsThreshold             | int     | RMS threshold (dB) to stop voice detection                          | -70     | [-100, 0]              |
| enableAdaptiveRmsThreshold   | boolean | Enable adaptive RMS threshold                                       | true    | true/false             |
| adaptiveRmsThresholdFactor   | float   | Adaptive RMS threshold factor                                       | 0.67    | [0.0, 1.0]             |

###### Parameter Description

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

###### Methods

```java
public synchronized VadProcessResult processFrame(AudioFrame frame)
```

- **Parameters**
  - `frame`: `AudioFrame` type, the audio frame.
- **Returns**
  - `VadProcessResult` type, the VAD processing result.

```java
public synchronized void destroy()
```

- Destroys the VAD module and releases resources.

##### VadProcessResult

Stores the result of VAD processing.

###### Constructor

```java
public VadProcessResult(byte[] result, Constants.VadState state)
```

- **Parameters**
  - `result`: `byte[]` type, the processed audio data.
  - `state`: `Constants.VadState` type, the current VAD state.

##### Usage Example

Here is a simple example showing how to use `AgoraAudioVadV2` for audio frame processing:

```java
import io.agora.rtc.AgoraAudioVadV2;
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.Constants;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.VadProcessResult;

public class VadV2Example {
    public static void main(String[] args) {
        // Create VAD configuration
        AgoraAudioVadConfigV2 config = new AgoraAudioVadConfigV2();
        // The following are default values, adjust as needed
        config.setPreStartRecognizeCount(16);
        config.setStartRecognizeCount(30);
        config.setStopRecognizeCount(65);
        config.setActivePercent(0.7f);
        config.setInactivePercent(0.5f);
        config.setStartVoiceProb(70);
        config.setStopVoiceProb(70);
        config.setStartRmsThreshold(-70);
        config.setStopRmsThreshold(-70);
        config.setEnableAdaptiveRmsThreshold(true);
        config.setAdaptiveRmsThresholdFactor(0.67f);

        // Create VAD instance
        AgoraAudioVadV2 vad = new AgoraAudioVadV2(config);

        // Simulate audio frame processing
        AudioFrame frame = new AudioFrame();
        // Set frame properties
        frame.setType(Constants.AudioFrameType.PCM16.getValue());
        frame.setSamplesPerSec(16000); // 16kHz
        frame.setChannels(1); // Mono
        frame.setSamplesPerChannel(160); // 10ms frame, 16000/100 = 160
        frame.setBytesPerSample(Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue()); // PCM16
        // Set audio data buffer
        byte[] pcmData = new byte[320]; // 160 samples * 1 channel * 2 bytes
        // ... Fill PCM data ...
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(320);
        buffer.put(pcmData);
        buffer.flip();
        frame.setBuffer(buffer);

        VadProcessResult result = vad.processFrame(frame);
        if (result != null) {
            System.out.println("VAD State: " + result.getState());
            if (result.getOutFrame() != null) {
                System.out.println("Processed Data Length: " + result.getOutFrame().length);
            }
        }

        // Destroy VAD instance
        vad.destroy();
    }
}
```

### Audio 3A Module (Only supported by Gateway SDK)

#### Introduction

The `AgoraAudioProcessor` is a module designed for Audio 3A processing and Background Human Voice Suppression (BGHVS), which includes Acoustic Echo Cancellation (AEC), Automatic Noise Suppression (ANS), Automatic Gain Control (AGC), and Background Human Voice Suppression (BGHVS). It processes audio frames to enhance audio quality by mitigating echo, reducing noise, normalizing volume levels, and suppressing background human voices. This module requires corresponding model files to perform its processing tasks.

#### Classes and Methods

##### AgoraAudioProcessor Class

###### Constructor

```java
public AgoraAudioProcessor()
```

- **Description**: Constructs an `AgoraAudioProcessor` instance.

###### Methods

```java
public int init(String appId, String license, IAgoraAudioProcessorEventHandler eventHandler, AgoraAudioProcessorConfig config)
```

- **Description**: Initializes the audio processor. This must be called before any other methods.
- **Parameters**:
  - `appId`: `String`, your App ID obtained from Agora Console.
  - `license`: `String`, your License key obtained from Agora Console.
  - `eventHandler`: `IAgoraAudioProcessorEventHandler`, a callback handler to receive processor events and errors.
  - `config`: `AgoraAudioProcessorConfig`, the 3A processor configuration object, used for setting the model path, etc.
- **Returns**: `int`, 0 for success, other values indicate failure.

```java
public AgoraAudioFrame process(AgoraAudioFrame nearIn)
```

- **Description**: Performs 3A processing (e.g., ANS, AGC) on the input near-end audio frame. Use this method when processing only near-end audio or when AEC is not required.
- **Parameters**:
  - `nearIn`: `io.agora.rtc.audio3a.AgoraAudioFrame`, the frame object containing near-end PCM audio data to be processed.
- **Returns**: `io.agora.rtc.audio3a.AgoraAudioFrame`, the processed audio frame. May return `null` if processing fails.

```java
public AgoraAudioFrame process(AgoraAudioFrame nearIn, AgoraAudioFrame farIn)
```

- **Description**: Performs 3A processing (e.g., AEC, ANS, AGC) on the input near-end and far-end audio frames. Use this method when Acoustic Echo Cancellation (AEC) or other processing that requires both near-end and far-end audio is needed.
- **Parameters**:
  - `nearIn`: `io.agora.rtc.audio3a.AgoraAudioFrame`, the frame object containing near-end PCM audio data to be processed.
  - `farIn`: `io.agora.rtc.audio3a.AgoraAudioFrame`, the frame object containing far-end PCM audio data for reference, primarily used for Acoustic Echo Cancellation (AEC).
- **Returns**: `io.agora.rtc.audio3a.AgoraAudioFrame`, the processed near-end audio frame. May return `null` if processing fails.

```java
public int release()
```

- **Description**: Releases all resources occupied by the `AgoraAudioProcessor` instance. This should be called when processing is complete.
- **Returns**: `int`, 0 for success, other values indicate failure.

##### AgoraAudioProcessorConfig Class

This class is used to configure the `AgoraAudioProcessor`.

###### Methods

```java
public void setModelPath(String modelPath)
```

- **Description**: Sets the path to the model files required for 3A processing. Model files are typically provided with the SDK package, often in a `resources/model/` directory.
- **Parameters**:
  - `modelPath`: `String`, the directory path where model files are located. For example, `./resources/model/`.

```java
public void setAecConfig(AecConfig aecConfig)
public AecConfig getAecConfig()
```

- **Description**: Sets and gets the Acoustic Echo Cancellation (AEC) configuration.
- **Parameters**:
  - `aecConfig`: `AecConfig` type, the AEC configuration object.

```java
public void setAnsConfig(AnsConfig ansConfig)
public AnsConfig getAnsConfig()
```

- **Description**: Sets and gets the Automatic Noise Suppression (ANS) configuration.
- **Parameters**:
  - `ansConfig`: `AnsConfig` type, the ANS configuration object.

```java
public void setAgcConfig(AgcConfig agcConfig)
public AgcConfig getAgcConfig()
```

- **Description**: Sets and gets the Automatic Gain Control (AGC) configuration.
- **Parameters**:
  - `agcConfig`: `AgcConfig` type, the AGC configuration object.

```java
public void setBghvsConfig(BghvsConfig bghvsConfig)
public BghvsConfig getBghvsConfig()
```

- **Description**: Sets and gets the Background Human Voice Suppression (BGHVS) configuration.
- **Parameters**:
  - `bghvsConfig`: `BghvsConfig` type, the BGHVS configuration object.

###### Example

```java
AgoraAudioProcessorConfig config = new AgoraAudioProcessorConfig();
config.setModelPath("./resources/model/"); // Set according to the actual model file location

// Configure AEC
AecConfig aecConfig = new AecConfig();
aecConfig.setEnabled(true);
config.setAecConfig(aecConfig);

// Configure ANS
AnsConfig ansConfig = new AnsConfig();
ansConfig.setEnabled(true);
config.setAnsConfig(ansConfig);

// Configure AGC
AgcConfig agcConfig = new AgcConfig();
agcConfig.setEnabled(true);
config.setAgcConfig(agcConfig);

// Configure BGHVS
BghvsConfig bghvsConfig = new BghvsConfig();
bghvsConfig.setEnabled(true);
config.setBghvsConfig(bghvsConfig);
```

##### IAgoraAudioProcessorEventHandler Interface

This interface is used to receive event and error notifications from the `AgoraAudioProcessor`.

###### Methods

```java
public void onEvent(Constants.AgoraAudioProcessorEventType eventType)
```

- **Description**: Reports events that occur during processor operation.
- **Parameters**:
  - `eventType`: `io.agora.rtc.Constants.AgoraAudioProcessorEventType`, the specific event type.

```java
public void onError(int errorCode)
```

- **Description**: Reports errors that occur during processor operation.
- **Parameters**:
  - `errorCode`: `int`, the error code indicating the specific error that occurred.


##### io.agora.rtc.audio3a.AgoraAudioFrame Class

This class is used to encapsulate audio data for processing by `AgoraAudioProcessor`. (Note: This might be different from `io.agora.rtc.AudioFrame`; use the version from the `audio3a` package).

###### Key Properties

| Property Name     | Type       | Description                                                                                                             |
| ----------------- | ---------- | ----------------------------------------------------------------------------------------------------------------------- |
| type              | int        | Audio frame type, typically `Constants.AudioFrameType.PCM16.getValue()`.                                                |
| sampleRate        | int        | Audio sample rate (Hz), e.g., 16000, 32000, 48000.                                                                      |
| channels          | int        | Number of audio channels, e.g., 1 (mono) or 2 (stereo).                                                                 |
| samplesPerChannel | int        | Number of samples per channel. For a 10ms frame, this is usually `sampleRate / 100`.                                    |
| bytesPerSample    | int        | Number of bytes per sample. E.g., for PCM16, it's 2 bytes (`Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue()`). |
| buffer            | ByteBuffer | `java.nio.ByteBuffer` containing the raw PCM audio data.                                                                |

###### Main Methods (Setters/Getters)

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

#### Usage Example

Below is a simple example demonstrating how to use `AgoraAudioProcessor` for audio frame processing:

```java
import io.agora.rtc.audio3a.AgoraAudioProcessor;
import io.agora.rtc.audio3a.AgoraAudioProcessorConfig;
import io.agora.rtc.audio3a.IAgoraAudioProcessorEventHandler;
import io.agora.rtc.audio3a.AgoraAudioFrame; // Use AgoraAudioFrame from the audio3a package
import io.agora.rtc.audio3a.AecConfig;
import io.agora.rtc.audio3a.AnsConfig;
import io.agora.rtc.audio3a.AgcConfig;
import io.agora.rtc.audio3a.BghvsConfig;
import io.agora.rtc.Constants; // SDK's constants class
import java.nio.ByteBuffer;
import java.util.Arrays; // For printing data in example

public class Audio3AProcessingExample {
    public static void main(String[] args) {
        // Replace with your App ID and License
        String appId = "YOUR_APP_ID";
        String license = "YOUR_LICENSE_KEY";

        // 1. Create AgoraAudioProcessor instance
        AgoraAudioProcessor audioProcessor = new AgoraAudioProcessor();

        // 2. Configure AgoraAudioProcessorConfig
        AgoraAudioProcessorConfig config = new AgoraAudioProcessorConfig();
        // Set the model file path, usually in resources/model/ of the SDK package
        // Ensure the path is correct, otherwise initialization might fail
        config.setModelPath("./resources/model/"); // Modify according to your actual path

        // Configure AEC (Acoustic Echo Cancellation)
        AecConfig aecConfig = config.getAecConfig();
        aecConfig.setEnabled(true); // Enable AEC
        
        // Configure ANS (Automatic Noise Suppression)
        AnsConfig ansConfig = config.getAnsConfig();
        ansConfig.setEnabled(true); // Enable ANS
        
        // Configure AGC (Automatic Gain Control)
        AgcConfig agcConfig = config.getAgcConfig();
        agcConfig.setEnabled(true); // Enable AGC
        
        // Configure BGHVS (Background Human Voice Suppression)
        BghvsConfig bghvsConfig = config.getBghvsConfig();
        bghvsConfig.setEnabled(true); // Enable BGHVS

        // 3. Initialize AgoraAudioProcessor
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
            // Handle initialization failure based on the error code, e.g., check appId, license, modelPath
            return;
        }
        System.out.println("AgoraAudioProcessor initialized successfully.");

        // 4. Prepare audio frame (AgoraAudioFrame)
        // Example parameters: 48kHz, mono, 10ms audio frame
        int sampleRate = 48000;
        int channels = 1;
        int samplesPerChannel = sampleRate / 100; // 10ms frame -> 480 samples
        int bytesPerSample = Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue(); // PCM16
        int bufferSize = samplesPerChannel * channels * bytesPerSample;

        // Create near-end audio frame
        AgoraAudioFrame nearInFrame = new AgoraAudioFrame();
        nearInFrame.setType(Constants.AudioFrameType.PCM16.getValue());
        nearInFrame.setSampleRate(sampleRate);
        nearInFrame.setChannels(channels);
        nearInFrame.setSamplesPerChannel(samplesPerChannel);
        nearInFrame.setBytesPerSample(bytesPerSample);
        // In a real application, pcmDataNear would come from a near-end audio source
        byte[] pcmDataNear = new byte[bufferSize]; 
        // ... Fill pcmDataNear with dummy data here ...
        ByteBuffer nearAudioBuffer = ByteBuffer.allocateDirect(bufferSize);
        nearAudioBuffer.put(pcmDataNear);
        nearAudioBuffer.flip();
        nearInFrame.setBuffer(nearAudioBuffer);

        // Create far-end audio frame (for AEC)
        AgoraAudioFrame farInFrame = new AgoraAudioFrame();
        farInFrame.setType(Constants.AudioFrameType.PCM16.getValue());
        farInFrame.setSampleRate(sampleRate);
        farInFrame.setChannels(channels);
        farInFrame.setSamplesPerChannel(samplesPerChannel);
        farInFrame.setBytesPerSample(bytesPerSample);
        // In a real application, pcmDataFar would come from a far-end audio source
        byte[] pcmDataFar = new byte[bufferSize]; 
        // ... Fill pcmDataFar with dummy data here ...
        ByteBuffer farAudioBuffer = ByteBuffer.allocateDirect(bufferSize);
        farAudioBuffer.put(pcmDataFar);
        farAudioBuffer.flip();
        farInFrame.setBuffer(farAudioBuffer);

        // 5. Process the audio frame
        // If you only need to process the near-end audio (e.g., only ANS, AGC), 
        // you can call the single-parameter process method:
        // AgoraAudioFrame outputFrame = audioProcessor.process(nearInFrame);

        // If AEC processing is required, pass both near-end and far-end audio frames
        AgoraAudioFrame outputFrame = audioProcessor.process(nearInFrame, farInFrame);

        if (outputFrame != null && outputFrame.getBuffer() != null) {
            System.out.println("Audio frame processed successfully.");
            ByteBuffer processedBuffer = outputFrame.getBuffer();
            // processedBuffer contains the 3A + BGHVS-processed audio data
            // The processed audio will have the following optimizations:
            // - AEC: Acoustic echo cancellation
            // - ANS: Background noise suppression
            // - AGC: Automatic gain control
            // - BGHVS: Background human voice suppression
            // You can write this data to a file, send it over the network, or perform other operations
            // Example: Get processed byte data:
            // byte[] processedBytes = new byte[processedBuffer.remaining()];
            // processedBuffer.get(processedBytes);
            // System.out.println("Processed data sample (first 10 bytes): " +
            // Arrays.toString(Arrays.copyOfRange(processedBytes, 0, Math.min(10, processedBytes.length))));
        } else {
            System.err.println("Failed to process audio frame or output frame is null.");
            // Check for error callbacks or the return value of the process method
        }

        // 6. Release resources
        int releaseRet = audioProcessor.release();
        if (releaseRet == 0) {
            System.out.println("AgoraAudioProcessor released successfully.");
        } else {
            System.err.println("Failed to release AgoraAudioProcessor. Error code: " + releaseRet);
        }
    }
}
```

## Changelog

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
