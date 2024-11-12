# Agora Java Server SDK

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

- [Maven](https://central.sonatype.com/artifact/io.agora.rtc/linux-java-sdk/overview)
- [Official Download Page](https://doc.shengwang.cn/doc/rtc-server-sdk/java/resources)

## API Examples

For detailed examples, please refer to [examples/README.md](examples/README.md)

## API Reference

### Basic API Reference

For complete API documentation, please visit [Agora Java Server SDK API Reference](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)

### AgoraAudioVadV2

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

### Latest Version: v4.4.30.1 (2024-11-12)

- Added AgoraAudioVad2 related Vad2 interfaces, removed AgoraAudioVad related Vad interfaces
- Introduced new callback interface IAudioEncodedFrameObserver for receiving encoded audio
- Fixed crash issues related to LocalAudioDetailedStats callbacks
- Modified parameter types for the onAudioVolumeIndication callback

### v4.4.30 (2024-10-24)

- For detailed update information, please refer to the [Release Notes](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes)

## FAQ

If you encounter any issues, please refer to the [Documentation Center](https://doc.shengwang.cn/) or search for related issues on [GitHub Issues](https://github.com/AgoraIO/Agora-Java-Server-SDK/issues)

## Support

- Technical Support: <sales@shengwang.cn>
- Business Inquiries: <sales@shengwang.cn>
- Other Architectural Support: <sales@shengwang.cn>
