# Agora Linux Java SDK Sample Guide

## Table of Contents

1. [Environment Setup](#environment-setup)
2. [Project Configuration](#project-configuration)
3. [Compilation Process](#compilation-process)
4. [Running Examples](#running-examples)
5. [Common Issues](#common-issues)

## Environment Setup

### Installing FFmpeg (Optional, required for MP4-related tests)

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

## Project Configuration

1. Create `examples/.keys` file and add:

   ```
   APP_ID=your_app_id
   TOKEN=your_token
   ```

   *If certificate is not enabled, TOKEN can be empty, for example:*

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

   Ensure the directory structure is as follows:

   ```
   libs/
   ├── agora-sdk.jar
   └── lib***.so
   ```

## Compilation Process

Execute the compilation script:

```bash
./build.sh [-ff]
```

- Use the `-ff` option to compile FFmpeg-related libraries (required for MP4 tests)

## Running Examples

1. Enter the examples directory:

   ```bash
   cd examples
   ```

2. Run the test script:

   ```bash
   ./script/TestCaseName.sh
   ```

3. Modify test parameters: directly edit the corresponding `.sh` file

## Common Issues

- Ensure Java environment is correctly installed and configured
- Verify `agora-sdk.jar` version compatibility
- Check `APP_ID` and `TOKEN` configuration before running
- Execute steps in order to avoid dependency issues
