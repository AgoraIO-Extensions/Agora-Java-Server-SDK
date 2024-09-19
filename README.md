# Agora-Java-Server-SDK

## Development Environment Requirements

Ensure your server meets the following requirements:

### Hardware Environment

#### Operating System

- Ubuntu (14.04 or later)
- CentOS (6.6 or later)

#### CPU Architecture

- arm64
- x86-64

> Note: For SDK integration on other architectures, please contact <sales@shengwang.cn>

#### Performance Requirements

- CPU: 8 cores at 1.8 GHz or higher
- Memory: 2 GB (4 GB or higher recommended)

#### Network Requirements

- The server must have public internet access and a public IP address.
- The server must be able to access the `.agora.io` and `.agoralab.co` domains.

### Software Environment

- Apache Maven or other build tools (this guide uses Apache Maven as an example)
- JDK 8

## Quick Start

### Running the Example Project

Please refer to the [official example documentation](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/run-example).

## Download SDK

[Official Download](https://doc.shengwang.cn/doc/rtc-server-sdk/java/resources)

[Latest Project Version](https://download.agora.io/sdk/release/linux-java-sdk-2.0_04af2fa340.zip)

## Testing API Examples

### Integrating the SDK

Place the downloaded SDK into the `examples/libs` directory.

### Configuring APP_ID and TOKEN

Create a `.keys` file in the `examples` directory and add the `APP_ID` and `TOKEN` values in the following format:

```
APP_ID=XXX
TOKEN=XXX
```

Note: If you do not have the corresponding values, leave them blank.

### Testing Steps

Using `MultipleConnectionPcmSendTest.sh` as an example, other tests can replace the corresponding `.sh` file:

Follow these steps to perform the test:

```bash
#!/bin/bash
set -e

cd examples
./build.sh
./script/ai/MultipleConnectionPcmSendTest.sh
```

1. **Integrate SDK**: Ensure the SDK is integrated in the `examples/libs` directory.
2. **Compile Examples**: Enter the `examples` directory and run `build.sh` to compile.
3. **Run Test**: Execute the `/script/ai/MultipleConnectionPcmSendTest.sh` script to run the test.

### Notes

- **Script Execution Order**: Ensure scripts are executed in the above order to avoid dependency issues.
- **Test Replacement**: To test other functions, simply replace the corresponding `.sh` file.

## VAD Usage

### Creating and Initializing a VAD Instance

1. Create a new VAD instance:

   ```java
   AgoraAudioVad audioVad = new AgoraAudioVad();
   ```

2. Initialize VAD configuration:

   ```java
   AgoraAudioVadConfig config = new AgoraAudioVadConfig();
   audioVad.initialize(config);
   ```

### Processing Audio Frames

Call the `processPcmFrame` method to process an audio frame. The frame should be 16-bit, 16 kHz, and mono PCM data:

```java
byte[] frame = // Get audio PCM data
VadProcessResult result = audioVad.processPcmFrame(frame);
```

### VAD Processing Results

`VadProcessResult` indicates the audio VAD processing result:

- `state` returns the current Voice Activity Detection (VAD) state:
  - `0` indicates no speech detected
  - `1` indicates speech start
  - `2` indicates ongoing speech
  - `3` indicates the end of the current speech segment

- If the function is in state `1`, `2`, or `3`, `outFrame` will contain PCM data corresponding to the VAD state.

### Handling ASR/TTS

When users want to perform ASR/TTS processing, they should send the `outFrame` data to the ASR system.

### Destroying the VAD Instance

When the VAD instance is no longer needed, call `audioVad.destroy()`:

```java
audioVad.destroy();
```

### Notes

- Release the VAD instance when the ASR system is no longer needed.
- One VAD instance corresponds to one audio stream.

## API Reference

### Basic API

- [Official API Reference](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)

### AgoraAudioVad Class

`AgoraAudioVad` is a management class for Voice Activity Detection (VAD). Through this class, you can process and analyze audio data.

#### Usage Steps

1. Call the `AgoraAudioVad` constructor to create an `AgoraAudioVad` object.
2. Use `AgoraAudioVadConfig` to configure the `AgoraAudioVad` object.

#### Member Functions

##### `int initialize(AgoraAudioVadConfig config)`

Configure the `AgoraAudioVad` object.

- **Parameters**
  - `config`: Configuration parameters (type `AgoraAudioVadConfig`)
- **Return Value**
  - `0`: Success
  - Non-`0`: Failure

##### `VadProcessResult processPcmFrame(byte[] frame)`

Process audio PCM data.

- **Parameters**
  - `frame`: Audio PCM data (byte array)
- **Return Value**
  - `VadProcessResult` object, containing:
    - `state`: Current Voice Activity Detection (VAD) state
    - `outFrame`: PCM data corresponding to the VAD state

##### `void destroy()`

Destroy the VAD instance.

#### Example Code

```java
AgoraAudioVad vad = new AgoraAudioVad();
AgoraAudioVadConfig config = new AgoraAudioVadConfig();
// Set configuration parameters
int result = vad.initialize(config);
if (result == 0) {
    byte[] audioFrame = // Get audio PCM data
    VadProcessResult processResult = vad.processPcmFrame(audioFrame);
    // Handle VAD results
}
vad.destroy();
```

## FAQ

## Changelog
