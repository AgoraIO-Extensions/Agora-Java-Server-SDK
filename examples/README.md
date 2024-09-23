# Examples

## Configuring APP_ID and TOKEN

Create a file named `.keys` in the `examples` directory and add the values for `APP_ID` and `TOKEN` in the following format:

```
APP_ID=XXX
TOKEN=XXX
```

> **Note**: If you do not have the corresponding values, you can leave them empty.

---

## Compilation Steps

1. **Add SDK JAR**:
   - Place the `agora-sdk.jar` file into the `libs` directory (if it does not exist, please create the `libs` folder manually).

2. **Extract SO Files**:
   - Extract the shared object (`.so`) files from `agora-sdk.jar` and place them in the `libs` directory. Use the following command to extract the contents:

   ```bash
   jar xvf agora-sdk.jar
   ```

   - The extracted `.so` files are usually located in the `native/linux/x86_64/` directory.

3. **Compile the Project**:
   - Run the following command to compile the project:

   ```bash
   ./build.sh
   ```

---

## Running Tests

1. **Enter the Examples Directory**:
   - Switch to the `examples` directory.

2. **Execute the Test Script**:
   - Use the following command to run the test script:

   ```bash
   ./script/ai/TestCashName.sh
   ```

3. **Modify Test Parameters**:
   - If you need to modify the test parameters, simply edit the corresponding `.sh` file.

> **Tip**: Make sure to follow the steps in the specified order to avoid any dependency issues.

---

## Additional Notes

- Ensure that you have the Java environment installed and properly configured.
- Make sure that the version of `agora-sdk.jar` is compatible with your project.
- Before running tests, confirm that `APP_ID` and `TOKEN` are correctly configured.
