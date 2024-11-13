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

| Property Name | Type | Description | Default Value | Range |
|---------------|------|-------------|---------------|-------|
| preStartRecognizeCount | int | Number of audio frames saved before starting speech state | 16 | [0, Integer.MAX_VALUE] |
| startRecognizeCount | int | Number of audio frames in speech state | 30 | [1, Integer.MAX_VALUE] |
| stopRecognizeCount | int | Number of audio frames in stop speech state | 20 | [1, Integer.MAX_VALUE] |
| activePercent | float | Percentage of active frames in startRecognizeCount frames | 0.7 | [0.0, 1.0] |
| inactivePercent | float | Percentage of inactive frames in stopRecognizeCount frames | 0.5 | [0.0, 1.0] |
| startVoiceProb | int | Probability threshold for starting voice detection | 70 | [0, 100] |
| stopVoiceProb | int | Probability threshold for stopping voice detection | 70 | [0, 100] |
| startRmsThreshold | int | RMS threshold for starting voice detection | -50 | [-100, 0] |
| stopRmsThreshold | int | RMS threshold for stopping voice detection | -50 | [-100, 0] |

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
