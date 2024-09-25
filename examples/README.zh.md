# Agora Linux Java SDK 示例指南

## 目录

1. [环境配置](#环境配置)
2. [项目设置](#项目设置)
3. [编译步骤](#编译步骤)
4. [运行测试](#运行测试)
5. [注意事项](#注意事项)

## 环境配置

### FFmpeg 开发库安装（可选）

> **注意**：如需测试 MP4 相关用例，此步骤为必须。

1. 更新系统软件包：

   ```bash
   sudo apt update
   ```

2. 安装 FFmpeg（需要 7.0 及以上版本）：

   ```bash
   sudo apt install ffmpeg
   ```

   *如果系统不支持 apt 直接安装，需要源码安装。*

3. 安装 FFmpeg 开发库：

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev
   ```

4. 查看库依赖路径：

   ```bash
   pkg-config --cflags --libs libavformat libavcodec libavutil libswresample libswscale
   ```

5. 根据第 4 步的输出更新 `build.sh` 中的 `FFMPEG_INCLUDE_DIR` 和 `FFMPEG_LIB_DIR` 路径。

## 项目设置

### 配置 APP_ID 和 TOKEN

1. 在 `examples` 目录下创建 `.keys` 文件。
2. 添加以下内容（替换 XXX 为实际值）：

   ```
   APP_ID=XXX
   TOKEN=XXX
   ```

   *如果未开启证书，TOKEN 值可为空，例如：*

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

## 编译步骤

1. **添加 SDK JAR**
   - 将 `agora-sdk.jar` 放入 `libs` 目录。

2. **提取 SO 文件**
   - 解压 `agora-sdk.jar`：

     ```bash
     jar xvf agora-sdk.jar
     ```

   - 将 `native/linux/x86_64/` 中的 `.so` 文件移至 `libs` 目录。

   目录结构应如下：

   ```
   libs/
   ├── agora-sdk.jar
   └── lib***.so
   ```

3. **编译项目**

   ```bash
   ./build.sh [选项]
   ```

   选项：
   - `-ff`：编译 FFmpeg 相关库（MP4 测试必需）
   - `-vad`：测试 VAD 库用例（必需）

   示例：
   - 测试 MP4 文件：`./build.sh -ff`
   - 测试 Vad：`./build.sh -vad`

## 运行测试

1. 进入示例目录：

   ```bash
   cd examples
   ```

2. 执行测试脚本：

   ```bash
   ./script/TestCaseName.sh
   ```

3. 修改测试参数：直接编辑对应的 `.sh` 文件。

## 注意事项

- 确保已安装 Java 环境并正确配置环境变量。
- 验证 `agora-sdk.jar` 版本与项目兼容。
- 运行测试前，确认 `APP_ID` 和 `TOKEN` 配置正确。
- 按顺序执行步骤，以避免依赖问题。
