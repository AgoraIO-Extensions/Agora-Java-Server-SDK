package io.agora.rtc.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static boolean areFilesIdentical(String file1Path, String file2Path) {
        try (FileInputStream fis1 = new FileInputStream(file1Path);
            FileInputStream fis2 = new FileInputStream(file2Path)) {
            // 比较文件大小
            if (fis1.available() != fis2.available()) {
                return false;
            }

            byte[] buffer1 = new byte[8192];
            byte[] buffer2 = new byte[8192];
            int bytesRead1, bytesRead2;

            while ((bytesRead1 = fis1.read(buffer1)) != -1) {
                bytesRead2 = fis2.read(buffer2);

                if (bytesRead1 != bytesRead2 || !Arrays.equals(buffer1, buffer2)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isLastCharDigit(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return Character.isDigit(str.charAt(str.length() - 1));
    }

    public static String formatTimestamp(long timestamp) {
        // 将时间戳转换为 Instant
        Instant instant = Instant.ofEpochMilli(timestamp);

        // 将 Instant 转换为 LocalDateTime,使用系统默认时区
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        // 使用 DateTimeFormatter 格式化 LocalDateTime
        return dtf.format(dateTime);
    }

    public static String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.now().format(dtf);
    }

    public static String[] readAppIdAndToken(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return new String[] {"", ""};
        }

        String appId = "";
        String token = "";

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            Pattern appIdPattern = Pattern.compile("APP_ID=(.*)");
            Pattern tokenPattern = Pattern.compile("TOKEN=(.*)");

            boolean foundAppId = false;
            boolean foundToken = false;

            for (String line : lines) {
                line = line.trim();

                if (!foundAppId) {
                    Matcher appIdMatcher = appIdPattern.matcher(line);
                    if (appIdMatcher.find()) {
                        appId = appIdMatcher.group(1).trim();
                        foundAppId = true;
                    }
                }

                if (!foundToken) {
                    Matcher tokenMatcher = tokenPattern.matcher(line);
                    if (tokenMatcher.find()) {
                        token = tokenMatcher.group(1).trim();
                        foundToken = true;
                    }
                }

                if (foundAppId && foundToken) {
                    break;
                }
            }

            if (!foundAppId || appId.isEmpty()) {
                return new String[] {"", ""};
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[] {appId, token};
    }

    public static String[] readAppIdAndLicense(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return new String[] {null, null};
        }
        String appId = null;
        String license = null;
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            Pattern appIdPattern = Pattern.compile("APP_ID=(.*)");
            Pattern licensePattern = Pattern.compile("LICENSE=(.*)");

            for (String line : lines) {
                Matcher appIdMatcher = appIdPattern.matcher(line);
                Matcher licenseMatcher = licensePattern.matcher(line);

                if (appIdMatcher.find()) {
                    appId = appIdMatcher.group(1);
                }
                if (licenseMatcher.find()) {
                    license = licenseMatcher.group(1);
                }

                if (appId != null && license != null) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[] {appId, license};
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static byte[] readPcmFromFile(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static void deleteAllFile(String filePath) {
        File directory = new File(filePath).getParentFile();

        if (directory != null && directory.isDirectory()) {
            File[] filesToDelete = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(new File(filePath).getName());
                }
            });

            if (filesToDelete != null) {
                for (File file : filesToDelete) {
                    file.delete();
                }
            }
        }
    }

    /**
     * Delete all files in the specified directory.
     *
     * @param directoryPath The path of the directory.
     * @return true if all files are deleted successfully, false otherwise.
     */
    public static boolean deleteAllFilesInDirectory(String directoryPath) {
        if (directoryPath == null || directoryPath.isEmpty()) {
            return false;
        }

        File directory = new File(directoryPath);

        // Check if the directory exists and is a directory
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }

        boolean allDeleted = true;
        for (File file : files) {
            if (file.isFile()) {
                if (!file.delete()) {
                    allDeleted = false;
                }
            }
        }

        return allDeleted;
    }

    public static boolean checkFileExists(String fileName) {
        return checkFileExists(fileName, "", "");
    }

    public static boolean checkFileExists(String fileName, String fileNameSuffix) {
        return checkFileExists(fileName, fileNameSuffix, "");
    }

    public static boolean checkFileExists(
        String fileName, String fileNameSuffix, String expectedFile) {
        List<File> files = getFilesInDirectory(fileName, fileNameSuffix);
        if (files.isEmpty()) {
            return false;
        }

        // Additional checks for specific file types
        for (File file : files) {
            String matchingFileName = file.getName().toLowerCase();

            // Check txt files - file size should be greater than 0
            if (matchingFileName.endsWith(".txt")) {
                if (file.length() <= 0) {
                    continue; // Skip this file if it's empty
                }
            }

            // Check pcm files - content should not be all zeros
            if (matchingFileName.endsWith(".pcm") || matchingFileName.endsWith(".yuv")) {
                if (isFileAllZeros(file)) {
                    continue; // Skip this file if content is all zeros
                }
            }

            // If we reach here, the file passed all checks
            return true;
        }

        if (expectedFile != null && !expectedFile.isEmpty()) {
            return areFilesIdentical(files.get(0).getAbsolutePath(), expectedFile);
        }

        return false;
    }

    public static List<File> getFilesInDirectory(String fileName, String fileNameSuffix) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        File file = new File(fileName);

        // Get parent directory and file name
        File parentDir = file.getParentFile();
        String fileNamePrefix = file.getName();

        // If there is no parent directory, use the current directory
        if (parentDir == null) {
            parentDir = new File(".");
        }

        // Check if the parent directory exists and is a directory
        if (!parentDir.exists() || !parentDir.isDirectory()) {
            return new ArrayList<>();
        }

        // Use FilenameFilter to find files starting with the specified prefix
        File[] matchingFiles = parentDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (fileNameSuffix.isEmpty()) {
                    return name.startsWith(fileNamePrefix);
                } else {
                    return name.startsWith(fileNamePrefix) && name.endsWith(fileNameSuffix);
                }
            }
        });

        // Return true if matching files are found
        if (matchingFiles == null || matchingFiles.length == 0) {
            return new ArrayList<>();
        }

        return Arrays.asList(matchingFiles);
    }

    public static String byteBufferToString(ByteBuffer buffer) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Check if a file contains all zeros.
     *
     * @param file The file to check.
     * @return true if the file contains all zeros, false otherwise.
     */
    private static boolean isFileAllZeros(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (buffer[i] != 0) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Write a string to a file.
     *
     * @param content  The string content to write.
     * @param filePath The path of the file to write to.
     * @return true if the string is written successfully, false otherwise.
     */
    public static boolean writeStringToFile(String content, String filePath) {
        if (content == null || filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            // Create parent directories if they don't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Write content to file
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Append a string to a file.
     *
     * @param content  The string content to append.
     * @param filePath The path of the file to append to.
     * @return true if the string is appended successfully, false otherwise.
     */
    public static boolean appendStringToFile(String content, String filePath) {
        if (content == null || filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            // Create parent directories if they don't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            // Append content to file
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeBytesToFile(byte[] bytes, String filePath) {
        if (bytes == null || filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        try {
            // Create parent directories if they don't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            Files.write(Paths.get(filePath), bytes, java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
