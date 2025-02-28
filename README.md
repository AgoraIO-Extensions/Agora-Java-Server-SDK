# Agora Linux Server Java SDK

[中文](README.zh.md) | English

## Table of Contents

- [System Requirements](#system-requirements)
- [Quick Start](#quick-start)
- [SDK Acquisition](#sdk-acquisition)
- [API Examples](#api-examples)
- [API Reference](#api-reference)
- [Changelog](#changelog)
- [FAQ](#faq)
- [Support](#support)

## System Requirements

### Hardware Requirements

- **Operating System**: Ubuntu 18.04+ or CentOS 7.0+
- **CPU Architecture**: x86-64
- **Performance Requirements**:
  - CPU: 8 cores at 1.8 GHz or higher
  - Memory: 2 GB (4 GB+ recommended)
- **Network Requirements**:
  - Public IP
  - Access to `.agora.io` and `.agoralab.co` domains

### Software Requirements

- Apache Maven or other build tools
- JDK 8+

## Quick Start

Refer to the [official example documentation](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/run-example)

## SDK Acquisition

### Maven download

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.31.3</version>
</dependency>
```

[linux-java-sdk-4.4.31.3](https://repo1.maven.org/maven2/io/agora/rtc/linux-java-sdk/4.4.31.3/linux-java-sdk-4.4.31.3.jar)

### CDN download

[Agora-Linux-Java-SDK-v4.4.31.3-x86_64-491956-06a9ee5318-20250227_132409](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.31.3-x86_64-491956-06a9ee5318-20250227_132409.jar)

## API Examples

### Environment Setup

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

1. Enter the `examples` directory:

   ```bash
   cd examples
   ```

2. Create `.keys` file and add:

   ```
   APP_ID=your_app_id
   TOKEN=your_token
   ```

   _If certificate is not enabled, TOKEN value can be empty, for example:_

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

3. Prepare SDK files:

   - Rename JAR to `agora-sdk.jar`
   - Place in `libs/` directory

4. Extract SO files:

   ```bash
   jar xvf agora-sdk.jar
   mv native/linux/x86_64/*.so libs/
   ```

   Ensure directory structure is:

   ```
   libs/
   ├── agora-sdk.jar
   └── lib***.so
   ```

### Compilation Process

Execute the build script:

```bash
./build.sh [-ffmpegUtils] [-mediaUtils]
```

- Use `-ffmpegUtils` option to compile FFmpeg related libraries (required for MP4 testing)
- Use `-mediaUtils` option to compile audio/video decoding libraries (required for encoded audio/video transmission tests)

### Running Examples

1. Execute test script:

   ```bash
   ./script/TestCaseName.sh
   ```

2. Modify test parameters: Directly edit the corresponding `.sh` file

### Test Cases

- Send PCM Audio

  Refer to `examples/src/java/io/agora/rtc/example/scenario/SendPcmFileTest.java` for loop sending PCM files

  Refer to `examples/src/java/io/agora/rtc/example/scenario/SendPcmRealTimeTest.java` for streaming PCM data transmission

- Send YUV Video

  Refer to `examples/src/java/io/agora/rtc/example/scenario/SendYuvTest.java` for streaming YUV data transmission

- Send H264 Video

  Refer to `examples/src/java/io/agora/rtc/example/scenario/SendH264Test.java` for streaming H264 data transmission

- Send Opus Audio

  Refer to `examples/src/java/io/agora/rtc/example/scenario/SendOpusTest.java` for streaming Opus data transmission

- Send MP4 Audio/Video

  Refer to `examples/src/java/io/agora/rtc/example/scenario/SendMp4Test.java` for MP4 file transmission

- Receive PCM Audio

  Refer to `examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmVadTest.java` for receiving PCM data with VAD information

  Refer to `examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmDirectSendTest.java` for receiving and directly resending PCM data

- Receive PCM&H264 Audio/Video

  Refer to `examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmH264Test.java` for receiving PCM&H264 data

- Receive PCM&YUV Audio/Video

  Refer to `examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmYuvTest.java` for receiving PCM&YUV data

### FAQ

- Ensure Java environment is properly installed and configured
- Verify `agora-sdk.jar` version compatibility
- Check `APP_ID` and `TOKEN` configuration before running
- Follow steps in order to avoid dependency issues

## API Reference

### Basic API Reference

For complete API documentation, please visit [Agora Java Server SDK API Reference](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)

### AgoraAudioVadV2 Class

#### Overview

`AgoraAudioVadV2` is a Voice Activity Detection (VAD) module used to process audio frames. It can detect voice activity in audio streams and handle them based on configuration parameters.

#### Classes and Methods

##### AgoraAudioVadV2

###### Constructor

```java
public AgoraAudioVadV2(AgoraAudioVadConfigV2 config)
```

- **Parameters**

  - `config`: `AgoraAudioVadConfigV2` type, VAD configuration.

  ###### AgoraAudioVadConfigV2 Properties

| Property Name          | Type  | Description                                                | Default Value | Range                  |
| ---------------------- | ----- | ---------------------------------------------------------- | ------------- | ---------------------- |
| preStartRecognizeCount | int   | Number of audio frames saved before starting speech state  | 16            | [0, Integer.MAX_VALUE] |
| startRecognizeCount    | int   | Number of audio frames in speech state                     | 30            | [1, Integer.MAX_VALUE] |
| stopRecognizeCount     | int   | Number of audio frames in stop speech state                | 20            | [1, Integer.MAX_VALUE] |
| activePercent          | float | Percentage of active frames in startRecognizeCount frames  | 0.7           | [0.0, 1.0]             |
| inactivePercent        | float | Percentage of inactive frames in stopRecognizeCount frames | 0.5           | [0.0, 1.0]             |
| startVoiceProb         | int   | Probability threshold for starting voice detection         | 70            | [0, 100]               |
| stopVoiceProb          | int   | Probability threshold for stopping voice detection         | 70            | [0, 100]               |
| startRmsThreshold      | int   | RMS threshold for starting voice detection                 | -50           | [-100, 0]              |
| stopRmsThreshold       | int   | RMS threshold for stopping voice detection                 | -50           | [-100, 0]              |

###### Notes

- `startVoiceProb`: The lower the value, the higher the probability that the frame is judged as active, and the earlier the start phase begins. Lower it for more sensitive voice detection.
- `stopVoiceProb`: The higher the value, the higher the probability that the frame is judged as inactive, and the earlier the stop phase begins. Increase it for quicker end of voice detection.
- `startRmsThreshold` and `stopRmsThreshold`:
  - The higher the value, the more sensitive to voice activity.
  - In quiet environments, the default value of -50 is recommended.
  - In noisy environments, it can be adjusted to between -40 and -30 to reduce false positives.
  - Fine-tune according to the actual usage scenario and audio characteristics for optimal results.

###### Methods

```java
public synchronized VadProcessResult processFrame(AudioFrame frame)
```

- **Parameters**
  - `frame`: `AudioFrame` type, the audio frame.
- **Returns**
  - `VadProcessResult` type, the result of the VAD process.

```java
public synchronized void destroy()
```

- Destroys the VAD module and releases resources.

##### VadProcessResult

Stores the VAD process result.

###### Constructor

```java
public VadProcessResult(byte[] result, Constants.VadState state)
```

- **Parameters**
  - `result`: `byte[]` type, the processed audio data.
  - `state`: `Constants.VadState` type, the current VAD state.

#### Usage Example

Here is a simple example demonstrating how to use `AgoraAudioVadV2` to process audio frames:

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
            System.out.println("Processed Data Length: " + result.getResult().length);
        }

        // Destroy VAD instance
        vad.destroy();
    }
}
```

## Changelog

### v4.4.31.3 (2025-02-26)

#### Optimization

- Fixed memory reuse issues that could cause exceptions.

### v4.4.31.2 (2025-02-19)

#### Optimization

- Optimized code handling to improve system robustness.

#### New Features

- Added `sendStreamMessage(int streamId, byte[] messageData)` method, deprecated `sendStreamMessage(int streamId, String message, int length)` method.

### v4.4.31.1 (2025-01-06)

#### Feature Optimization

- Optimized VAD functionality configuration, now VAD functionality is enabled by default without manual configuration

### v4.4.31 (2024-12-23)

#### New Features

- Added `DomainLimit` configuration option in `AgoraServiceConfig` for domain restriction management.
- Added `VadDumpUtils` utility class to support exporting VAD process debug data for troubleshooting.
- Added `AudioConsumerUtils` class, providing optimized PCM data transmission mechanism to effectively prevent audio distortion.
- Modified `registerAudioFrameObserver` method in `AgoraLocalUser` to support `AgoraAudioVadConfigV2` parameter configuration.
- Added `vadResult` parameter in `onPlaybackAudioFrameBeforeMixing` callback of `IAudioFrameObserver` to provide more detailed VAD processing results.
- Added `sendAudioMetaData` method in `AgoraLocalUser` class for sending audio metadata.
- Added `onAudioMetaDataReceived` callback in `ILocalUserObserver` class for receiving audio metadata.
- Added `ColorSpace` property in the `ExternalVideoFrame` class to support custom color space configuration.

#### Performance Improvements

- Optimized code logic architecture to significantly improve memory efficiency.
- Fixed multiple memory leak issues to enhance system stability.
- Enhanced memory access security mechanism to effectively prevent memory corruption.

### v4.4.30.2 (2024-11-20)

- Enhanced the `processFrame` handling in `AgoraAudioVadV2` with new `START_SPEAKING` and `STOP_SPEAKING` state callbacks.
- Improved parameter types for encoded frame callbacks. `onEncodedAudioFrameReceived`, `onEncodedVideoImageReceived`, and `onEncodedVideoFrame` now use `ByteBuffer` instead of `Byte` arrays, enhancing performance and flexibility.
- Optimized VAD plugin startup; `enableExtension` is now implemented within the SDK, so applications no longer need to call this method manually.
- Fixed issues with the handling of `alphaBuffer` and `metadataBuffer` in `VideoFrame`.

#### Developer Notes

- Please update the code using encoded frame callbacks to accommodate the new `ByteBuffer` parameter type.
- If you previously called the `enableExtension` method for the VAD plugin manually, you can now remove that call.

### v4.4.30.1 (2024-11-12)

- Added `Vad2` interfaces related to `AgoraAudioVad2` and removed `Vad` interfaces related to `AgoraAudioVad`.
- Added a new callback interface for receiving encoded audio frames: `IAudioEncodedFrameObserver`.
- Fixed crashes related to `LocalAudioDetailedStats` callbacks.
- Modified the parameter types for the `onAudioVolumeIndication` callback.

### v4.4.30 (2024-10-24)

- For detailed release notes, please refer to the [Release Notes](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes).

## FAQ

If you encounter any issues, please refer to the [Documentation Center](https://doc.shengwang.cn/) or search for related issues on [GitHub Issues](https://github.com/AgoraIO/Agora-Java-Server-SDK/issues)

## Support

- Technical Support: <sales@shengwang.cn>
- Business Inquiries: <sales@shengwang.cn>
- Other Architectural Support: <sales@shengwang.cn>
