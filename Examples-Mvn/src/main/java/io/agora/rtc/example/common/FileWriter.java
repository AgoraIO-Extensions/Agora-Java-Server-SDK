package io.agora.rtc.example.common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileWriter {
    private static final int MAX_FILE_BYTES = 100 * 1024 * 1024;
    private static final int BUFF_SIZE = 8192;
    private String filePath;
    private FileOutputStream fos;
    int fileCount;
    int fileSize_;
    private byte[] dataBuffer = new byte[8192];

    public FileWriter(String path) {
        filePath = path;
    }

    private void checkState() {
        if (fos == null) {
            // String fileName = (++fileCount > 1)
            // ? (filePath + fileCount)
            // : filePath;

            try {
                fos = new FileOutputStream(filePath, true);
                // SampleLogger.log("Created file to save samples fielName:" +
                // filePath);
            } catch (FileNotFoundException e) {
                SampleLogger.log("Open file fail");
            }
        }
    }

    public void writeData(ByteBuffer buffer, int writeBytes) {
        checkState();
        // Write PCM samples
        try {
            if (!buffer.isDirect()) {
                fos.write(buffer.array());
            } else {
                int length = 0;
                int writableSize = 0;
                while ((length = buffer.remaining()) > 0) {
                    writableSize = length > BUFF_SIZE ? BUFF_SIZE : length;
                    buffer.get(dataBuffer, 0, writableSize);
                    fos.write(dataBuffer, 0, writableSize);
                }
            }
            fileSize_ += writeBytes;
            // Close the file if size limit is reached
            if (fileSize_ >= MAX_FILE_BYTES) {
                fos.close();
                fos = null;
                fileSize_ = 0;
            }
        } catch (IOException e) {
            SampleLogger.log("file write exception");
        }
    }

    public void release() {
        if (fos != null) {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fos = null;
        }
    }

    public void writeData(byte[] buffer, int writeBytes) {
        checkState();
        // Write PCM samples
        try {
            fos.write(buffer);
            fileSize_ += writeBytes;
            // Close the file if size limit is reached
            if (fileSize_ >= MAX_FILE_BYTES) {
                fos.close();
                fos = null;
                fileSize_ = 0;
            }
        } catch (IOException e) {
            SampleLogger.log("file write exception");
        }
    }

    public void writeData(byte[] buffer, int writeBytes, String path) {
        filePath = path;
        checkState();
        try {
            fos.write(buffer);
            release();
        } catch (IOException e) {
            SampleLogger.log("file write exception");
        }
    }

    public int getFileSize() {
        return fileSize_;
    }
}
