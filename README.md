# Agora Linux Server Java SDK

[中文](./README.zh.md) | English

## Table of Contents

- [Introduction](#introduction)
- [Development Environment Requirements](#development-environment-requirements)
- [SDK Download](#sdk-download)
- [Quick Start](#quick-start)
- [Integrate the SDK](#integrate-the-sdk)
- [Examples](#examples)
- [API Reference](#api-reference)
- [Changelog](#changelog)
- [Other References](#other-references)

## Introduction

The Agora Linux Server Java SDK (v4.4.31.4) provides powerful real-time audio and video communication capabilities that can be seamlessly integrated into Linux server-side Java applications. With this SDK, your server can join Agora channels as a data source or processing node, accessing and processing audio and video streams in real-time to implement various business-related advanced features.

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
    <version>4.4.31.4</version>
</dependency>
```

### CDN Download

[Agora-Linux-Java-SDK-v4.4.31.4-x86_64-491956-341b4be9b9-20250402_171133](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.31.4-x86_64-491956-341b4be9b9-20250402_171133.zip)

## Quick Start

Refer to the [Official Example Documentation](https://docs.agora.io/en/rtc-server-sdk/java/get-started/run-example/)

### Enable Service

Refer to [Official Service Activation Guide](https://docs.agora.io/en/rtc-server-sdk/java/get-started/enable-service/)

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
    <version>4.4.31.4</version>
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
  -Dversion=4.4.31.4 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

Method 2: Install both SDK JAR and JavaDoc JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.31.4 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -Djavadoc=sdk/agora-sdk-javadoc.jar
```

After installation, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.31.4</version>
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

### Loading Native Libraries (.so Files)

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
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-java-sdk/4.4.31.4/linux-java-sdk-4.4.31.4.jar
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

## Examples

### Environment Preparation

#### Install FFmpeg (Optional, for MP4 related tests)

1. Update system packages:

   ```bash
   sudo apt update
   ```

2. Install FFmpeg (version 7.0+ required):

   ```bash
   sudo apt install ffmpeg
   ```

3. Install FFmpeg development libraries:

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev
   ```

4. Get library dependency paths:

   ```bash
   pkg-config --cflags --libs libavformat libavcodec libavutil libswresample libswscale
   ```

5. Update `FFMPEG_INCLUDE_DIR` and `FFMPEG_LIB_DIR` in `build.sh`.

### Project Configuration

1. Go to the `Examples` directory:

   ```bash
   cd Examples
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

3. Prepare the SDK file:

   - Rename the JAR to `agora-sdk.jar`
   - Place it in the `libs/` directory

4. Extract SO files:

   Go to the `Examples/libs` directory and execute:

   ```bash
   jar xvf agora-sdk.jar
   ```

   After extraction, ensure the directory structure is as follows:

   ```
   libs/
   ├── agora-sdk.jar
   └── native/
       └── linux/
           └── x86_64/
               └── lib*.so (various dynamic library files)
   ```

   Note: Ensure all .so files are correctly extracted as they are core components of the SDK.

### Compilation Process

Execute the build script:

```bash
./build.sh [-ffmpegUtils] [-mediaUtils]
```

- Use the `-ffmpegUtils` option to compile FFmpeg related libraries (required for MP4 tests).
- Use the `-mediaUtils` option to compile audio/video decoding related libraries (required for sending encoded audio/video tests).

### Running Examples

1. Run the test script:

   ```bash
   ./script/TestCaseName.sh
   ```

2. Modify test parameters: Directly edit the corresponding `.sh` file.

### Test Cases

- Send PCM Audio

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/SendPcmFileTest.java` for sending PCM files in a loop.

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/SendPcmRealTimeTest.java` for sending streaming PCM data.

- Send YUV Video

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/SendYuvTest.java` for sending streaming YUV data.

- Send H264 Video

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/SendH264Test.java` for sending streaming H264 data.

- Send Opus Audio

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/SendOpusTest.java` for sending streaming Opus data.

- Send MP4 Audio/Video

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/SendMp4Test.java` for sending MP4 files.

- Receive PCM Audio

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmVadTest.java` for receiving PCM data with VAD information.

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmDirectSendTest.java` for receiving PCM data and sending it back directly.

- Receive PCM & H264 Audio/Video

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmH264Test.java` for receiving PCM & H264 data.

- Receive PCM & YUV Audio/Video

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmYuvTest.java` for receiving PCM & YUV data.

- Send/Receive Stream Messages

  Refer to `Examples/src/java/io/agora/rtc/example/scenario/SendReceiverStreamMessageTest.java` for sending and receiving stream messages.

### Common Issues

- Ensure the Java environment is correctly installed and configured.
- Verify `agora-sdk.jar` version compatibility.
- Check `APP_ID` and `TOKEN` configuration before running.
- Follow the steps sequentially to avoid dependency issues.

## API Reference

### API Documentation Reference

For complete API documentation, refer to the following resources:

- [API-reference.md](./API-reference.md) file (For reference only, based on the Chinese version)
- Official Documentation [Agora Java Server SDK API Reference](https://docs.agora.io/en/rtc-server-sdk/java/reference/api-overview/) (Official documentation takes precedence)

### VAD Module

#### Introduction

`AgoraAudioVadV2` is a Voice Activity Detection (VAD) module for processing audio frames. It detects speech activity in an audio stream and processes it based on configuration parameters.

#### Classes and Methods

##### AgoraAudioVadV2 Class

###### Constructor

```java
public AgoraAudioVadV2(AgoraAudioVadConfigV2 config)
```

- **Parameters**
  - `config`: `AgoraAudioVadConfigV2` type, VAD configuration.

###### AgoraAudioVadConfigV2 Properties

| Property Name          | Type  | Description                                                         | Default | Range                  |
| ---------------------- | ----- | ------------------------------------------------------------------- | ------- | ---------------------- |
| preStartRecognizeCount | int   | Number of audio frames saved before entering speaking state         | 16      | [0, Integer.MAX_VALUE] |
| startRecognizeCount    | int   | Number of audio frames to confirm speaking state                    | 30      | [1, Integer.MAX_VALUE] |
| stopRecognizeCount     | int   | Number of audio frames to confirm stop speaking state               | 20      | [1, Integer.MAX_VALUE] |
| activePercent          | float | Percentage of active frames required in startRecognizeCount frames  | 0.7     | [0.0, 1.0]             |
| inactivePercent        | float | Percentage of inactive frames required in stopRecognizeCount frames | 0.5     | [0.0, 1.0]             |
| startVoiceProb         | int   | Probability threshold to start voice detection                      | 70      | [0, 100]               |
| stopVoiceProb          | int   | Probability threshold to stop voice detection                       | 70      | [0, 100]               |
| startRmsThreshold      | int   | RMS threshold (dBFS) to start voice detection                       | -50     | [-100, 0]              |
| stopRmsThreshold       | int   | RMS threshold (dBFS) to stop voice detection                        | -50     | [-100, 0]              |

###### Notes

- `startVoiceProb`: Lower value means higher probability of a frame being judged active, starting the phase earlier. Lower it for more sensitive detection.
- `stopVoiceProb`: Higher value means higher probability of a frame being judged inactive, ending the phase earlier. Raise it for quicker end detection.
- `startRmsThreshold` & `stopRmsThreshold`:
  - Higher value means more sensitive to voice activity.
  - Default -50 is recommended for quiet environments.
  - In noisy environments, adjust between -40 and -30 to reduce false positives.
  - Fine-tune based on the actual usage scenario and audio characteristics for optimal results.

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

#### Usage Example

Here is a simple example showing how to use `AgoraAudioVadV2` for audio frame processing:

```java
import io.agora.rtc.AgoraAudioVadV2;
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.Constants;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.VadProcessResult;

public class Main {
    public static void main(String[] args) {
        // Create VAD configuration
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

        // Create VAD instance
        AgoraAudioVadV2 vad = new AgoraAudioVadV2(config);

        // Simulate audio frame processing
        AudioFrame frame = new AudioFrame();
        // Set frame properties...

        VadProcessResult result = vad.processFrame(frame);
        if (result != null) {
            System.out.println("VAD State: " + result.getState());
            // Assuming getResult() exists on VadProcessResult (based on constructor)
            // System.out.println("Processed Data Length: " + result.getResult().length);
            System.out.println("Processed Data Length: " + result.getOutFrame().length); // Using getOutFrame based on API Ref
        }

        // Destroy VAD instance
        vad.destroy();
    }
}
```

## Changelog

### v4.4.31.4 (2025-03-21)

#### Improvements & Optimizations

- Fixed potential crashes caused by exceptions in multi-threaded environments.
- Improved error handling processes, enhancing recovery capabilities in exceptional circumstances.

### v4.4.31.3 (2025-02-26)

#### Improvements & Optimizations

- Fixed exception handling issues potentially caused by memory reuse.

### v4.4.31.2 (2025-02-19)

#### API Changes

- Added `sendStreamMessage(int streamId, byte[] messageData)` method, deprecated `sendStreamMessage(int streamId, String message, int length)` method.

#### Improvements & Optimizations

- Optimized code handling, improving system robustness.

### v4.4.31.1 (2025-01-06)

#### Improvements & Optimizations

- Optimized VAD function configuration; VAD is now enabled by default, manual configuration is not required.

### v4.4.31 (2024-12-23)

#### API Changes

- Added `DomainLimit` configuration option in `AgoraServiceConfig` for domain limit management.
- Added `VadDumpUtils` utility class, supporting export of debug data from the VAD process.
- Added `AudioConsumerUtils` class, providing an optimized PCM data transmission mechanism.
- Modified `registerAudioFrameObserver` method in `AgoraLocalUser` to support configuration of `AgoraAudioVadConfigV2` parameters.
- Added `vadResult` parameter to the `onPlaybackAudioFrameBeforeMixing` callback in `IAudioFrameObserver`.
- Added `sendAudioMetaData` method in `AgoraLocalUser` class to support sending audio metadata.
- Added `onAudioMetaDataReceived` callback in `ILocalUserObserver` class for receiving audio metadata.
- Added `ColorSpace` property in `ExternalVideoFrame` class to support custom color space settings.

#### Improvements & Optimizations

- Optimized code logic architecture, significantly improving memory usage efficiency.
- Fixed multiple memory leak issues, enhancing system stability.
- Strengthened memory access security mechanisms, effectively preventing memory corruption issues.

### v4.4.30.2 (2024-11-20)

#### API Changes

- Enhanced `processFrame` handling in AgoraAudioVadV2, adding `START_SPEAKING` and `STOP_SPEAKING` state callbacks.
- Improved parameter types for encoded frame callbacks: `onEncodedAudioFrameReceived`, `onEncodedVideoImageReceived`, `onEncodedVideoFrame` now use `ByteBuffer` instead of `Byte` arrays.

#### Improvements & Optimizations

- VAD plugin startup optimization: `enableExtension` is now handled internally by the SDK; applications no longer need to call this method manually.
- Fixed handling issues with `alphaBuffer` and `metadataBuffer` in `VideoFrame`.

### v4.4.30.1 (2024-11-12)

#### API Changes

- Added AgoraAudioVad2 related `Vad2` interface, removed AgoraAudioVad related `Vad` interface.
- Added callback interface `IAudioEncodedFrameObserver` for receiving encoded audio.

#### Improvements & Optimizations

- Fixed crash issues related to `LocalAudioDetailedStats` callbacks.
- Modified parameter types for the `onAudioVolumeIndication` callback.

### v4.4.30 (2024-10-24)

- For detailed changelog, please refer to the [Release Notes](https://docs.agora.io/en/rtc-server-sdk/java/overview/release-notes/)

## Other References

Refer to the official website for details (<https://docs.agora.io/en/rtc-server-sdk/java/landing-page/>)

Official API documentation [Agora Server Java SDK API Reference](https://docs.agora.io/en/rtc-server-sdk/java/reference/api-overview/)
