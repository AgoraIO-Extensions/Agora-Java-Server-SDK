# Agora Linux Java SDK Sample Guide

## Table of Contents

1. [Environment Setup](#environment-setup)
2. [Project Configuration](#project-configuration)
3. [Build Process](#build-process)
4. [Running Examples](#running-examples)
5. [Troubleshooting](#troubleshooting)

## Environment Setup

### Installing FFmpeg (Optional, for MP4 related testing)

1. Update system packages:

   ```bash
   sudo apt update
   ```

2. Install FFmpeg (requires 7.0+):

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

## Project Configuration

1. Create `examples/.keys` file and add:

   ```
   APP_ID=your_app_id
   TOKEN=your_token
   ```

   *If certificate is not enabled, TOKEN value can be empty, for example:*

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

2. Prepare SDK files:
   - Rename JAR to `agora-sdk.jar`
   - Place it in the `libs/` directory

3. Extract SO files:

   ```bash
   jar xvf agora-sdk.jar
   mv native/linux/x86_64/*.so libs/
   ```

   Ensure directory structure is as follows:

   ```
   libs/
   ├── agora-sdk.jar
   └── lib***.so
   ```

## Build Process

Execute build script:

```bash
./build.sh [-ffmpegUtils] [-mediaUtils]
```

- Use `-ffmpegUtils` option to build FFmpeg related libraries (required for MP4 testing)
- Use `-mediaUtils` option to build decode audio/video related libraries (required for sending encoded audio/video testing)

## Running Examples

1. Navigate to examples directory:

   ```bash
   cd examples
   ```

2. Run test script:

   ```bash
   ./script/TestCaseName.sh
   ```

3. Modify test parameters: directly edit corresponding `.sh` file

## Troubleshooting

- Ensure Java environment is correctly installed and configured
- Verify `agora-sdk.jar` version compatibility
- Check `APP_ID` and `TOKEN` configuration before running
- Follow steps in order to avoid dependency issues
