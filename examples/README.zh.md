# Agora Linux Java SDK 示例指南

## 目录

1. [环境准备](#环境准备)
2. [项目配置](#项目配置)
3. [编译过程](#编译过程)
4. [运行示例](#运行示例)
5. [常见问题](#常见问题)

## 环境准备

### 安装 FFmpeg（可选，用于 MP4 相关测试）

1. 更新系统包：

   ```bash
   sudo apt update
   ```

2. 安装 FFmpeg（需要 7.0+）：

   ```bash
   sudo apt install ffmpeg
   ```

3. 安装 FFmpeg 开发库：

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev
   ```

4. 获取库依赖路径：

   ```bash
   pkg-config --cflags --libs libavformat libavcodec libavutil libswresample libswscale
   ```

5. 更新 `build.sh` 中的 `FFMPEG_INCLUDE_DIR` 和 `FFMPEG_LIB_DIR`。

## 项目配置

1. 创建 `examples/.keys` 文件，添加：

   ```
   APP_ID=your_app_id
   TOKEN=your_token
   ```

   *如果未开启证书，TOKEN 值可为空，例如：*

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

2. 准备 SDK 文件：
   - 重命名 JAR 为 `agora-sdk.jar`
   - 放入 `libs/` 目录

3. 提取 SO 文件：

   ```bash
   jar xvf agora-sdk.jar
   mv native/linux/x86_64/*.so libs/
   ```

   确保目录结构如下：

   ```
   libs/
   ├── agora-sdk.jar
   └── lib***.so
   ```

## 编译过程

执行编译脚本：

```bash
./build.sh [-ff]
```

- 使用 `-ff` 选项编译 FFmpeg 相关库（MP4 测试必需）

## 运行示例

1. 进入示例目录：

   ```bash
   cd examples
   ```

2. 运行测试脚本：

   ```bash
   ./script/TestCaseName.sh
   ```

3. 修改测试参数：直接编辑对应的 `.sh` 文件

## 常见问题

- 确保 Java 环境正确安装和配置
- 验证 `agora-sdk.jar` 版本兼容性
- 运行前检查 `APP_ID` 和 `TOKEN` 配置
- 按顺序执行步骤，避免依赖问题
