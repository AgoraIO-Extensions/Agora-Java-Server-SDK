# Agora Linux Java SDK Example Guide

## Table of Contents

1. [Environment Setup](#environment-setup)
2. [Project Configuration](#project-configuration)
3. [Compilation Steps](#compilation-steps)
4. [Running Tests](#running-tests)
5. [Important Notes](#important-notes)

## Environment Setup

### FFmpeg Development Library Installation (Optional)

> **Note**: This step is mandatory if you need to test MP4-related cases.

1. Update system packages:

   ```bash
   sudo apt update
   ```

2. Install FFmpeg (version 7.0 or above required):

   ```bash
   sudo apt install ffmpeg
   ```

   *If your system doesn't support direct installation via apt, you'll need to install from source.*

3. Install FFmpeg development libraries:

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev
   ```

4. Check library dependency paths:

   ```bash
   pkg-config --cflags --libs libavformat libavcodec libavutil libswresample libswscale
   ```

5. Update `FFMPEG_INCLUDE_DIR` and `FFMPEG_LIB_DIR` paths in `build.sh` based on the output from step 4.

## Project Configuration

### Configure APP_ID and TOKEN

1. Create a `.keys` file in the `examples` directory.
2. Add the following content (replace XXX with actual values):

   ```
   APP_ID=XXX
   TOKEN=XXX
   ```

   *If the certificate is not enabled, the TOKEN value can be empty, for example:*

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

## Compilation Steps

1. **Add SDK JAR**
   - Place `agora-sdk.jar` in the `libs` directory.

2. **Extract SO Files**
   - Unzip `agora-sdk.jar`:

     ```bash
     jar xvf agora-sdk.jar
     ```

   - Move `.so` files from `native/linux/x86_64/` to the `libs` directory.

   The directory structure should look like this:

   ```
   libs/
   ├── agora-sdk.jar
   └── lib***.so
   ```

3. **Compile the Project**

   ```bash
   ./build.sh [options]
   ```

   Options:
   - `-ff`: Compile FFmpeg-related libraries (required for MP4 testing)
   - `-vad`: Test VAD library cases (required)

   Examples:
   - To test MP4 files: `./build.sh -ff`
   - To test Vad: `./build.sh -vad`

## Running Tests

1. Enter the examples directory:

   ```bash
   cd examples
   ```

2. Execute the test script:

   ```bash
   ./script/TestCaseName.sh
   ```

3. Modify test parameters: Edit the corresponding `.sh` file directly.

## Important Notes

- Ensure Java environment is installed and properly configured.
- Verify that the `agora-sdk.jar` version is compatible with your project.
- Confirm that `APP_ID` and `TOKEN` are correctly configured before running tests.
- Follow the steps in order to avoid dependency issues.
