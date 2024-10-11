package io.agora.rtc.common;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            return new String[] { null, null };
        }
        String appId = null;
        String token = null;
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            Pattern appIdPattern = Pattern.compile("APP_ID=(.*)");
            Pattern tokenPattern = Pattern.compile("TOKEN=(.*)");

            for (String line : lines) {
                Matcher appIdMatcher = appIdPattern.matcher(line);
                Matcher tokenMatcher = tokenPattern.matcher(line);

                if (appIdMatcher.find()) {
                    appId = appIdMatcher.group(1);
                }
                if (tokenMatcher.find()) {
                    token = tokenMatcher.group(1);
                }

                if (appId != null && token != null) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[] { appId, token };
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
}
