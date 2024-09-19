# Examples

## Configure APP_ID and TOKEN

Create a file named `.keys` in the `examples` directory and add the `APP_ID` and `TOKEN` values in the following format:

```
APP_ID=XXX
TOKEN=XXX
```

> **Note**: If you do not have the corresponding values, you can leave them blank.

## Compilation Steps

1. **Remove Old Library Files**:
   - Delete all `.jar` and `.so` files from the `libs` directory, except for `libmediautils.so`.

2. **Add SDK JAR**:
   - Place the `agora-sdk.jar` file into the `libs` directory.

3. **Extract SO Files**:
   - Extract the shared object (`.so`) files from `agora-sdk.jar` and place them in the `libs` directory. Use the following command to extract the contents:

   ```bash
   jar xvf agora-sdk.jar
   ```

   - The extracted `.so` files are typically located in the `native/linux/x86_64/` directory.

4. **Compile the Project**:
   - Run the following command to compile the project:

   ```bash
   ./build.sh
   ```

## Run Tests

1. **Enter the Examples Directory**:
   - Switch to the `examples` directory.

2. **Execute the Test Script**:
   - Run the test script using the following command:

   ```bash
   ./script/ai/TestCashName.sh
   ```

3. **Modify Test Parameters**:
   - If you need to change the test parameters, simply edit the corresponding `.sh` file.

> **Tip**: Please ensure to follow the steps in the specified order to avoid any dependency issues.
