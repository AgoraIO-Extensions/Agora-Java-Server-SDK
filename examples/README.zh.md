# Examples

## 配置 APP_ID 和 TOKEN

在 `examples` 目录下创建一个名为 `.keys` 的文件，并按照以下格式添加 `APP_ID` 和 `TOKEN` 的值：

```
APP_ID=XXX
TOKEN=XXX
```

> **注意**：如果没有对应的值，可以将其留空。

## 编译步骤

1. **删除旧的库文件**：
   - 从 `libs` 目录中删除所有 `.jar` 和 `.so` 文件，除了 `libmediautils.so`。

2. **添加 SDK JAR**：
   - 将 `agora-sdk.jar` 文件放入 `libs` 目录。

3. **提取 SO 文件**：
   - 从 `agora-sdk.jar` 中提取共享对象（`.so`）文件，并将它们放入 `libs` 目录。使用以下命令提取内容：

   ```bash
   jar xvf agora-sdk.jar
   ```

   - 提取后的 `.so` 文件通常位于 `native/linux/x86_64/` 目录中。

4. **编译项目**：
   - 运行以下命令以编译项目：

   ```bash
   ./build.sh
   ```

## 运行测试

1. **进入示例目录**：
   - 切换到 `examples` 目录。

2. **执行测试脚本**：
   - 使用以下命令运行测试脚本：

   ```bash
   ./script/ai/TestCashName.sh
   ```

3. **修改测试参数**：
   - 如果需要修改测试参数，直接编辑相应的 `.sh` 文件即可。

> **提示**：请确保按照上述步骤的顺序执行，以避免任何依赖问题。
