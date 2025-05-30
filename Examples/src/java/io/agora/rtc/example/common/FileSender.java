package io.agora.rtc.example.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class FileSender implements Runnable {
    private String filePath;
    private int interval;// in ms
    private boolean sending = false;
    private FileInputStream fos;
    // file handled by FileSender or not; H264Sender handle file by itself.
    private boolean handleFile = true;

    public FileSender(String filePath, int interval) {
        this.filePath = filePath;
        this.interval = interval;
    }

    public FileSender(String filePath, int interval, boolean handleFile) {
        this.filePath = filePath;
        this.interval = interval;
        this.handleFile = handleFile;
    }

    @Override
    public void run() {
        if (handleFile) {
            try {
                fos = new FileInputStream(new File(filePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                SampleLogger.log("cannot open file :" + filePath);
                return;
            }
        }
        long lastSendTime = 0;
        long nextSendTime = System.currentTimeMillis();
        long sendPiece = 0;
        sending = true;
        while (sending) {
            byte[] data = readOneFrame(fos);
            try {
                while (System.currentTimeMillis() < nextSendTime) {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                SampleLogger.log("FileSender thread interrupted, stopping.");
                sending = false; // Ensure loop terminates on interruption
                Thread.currentThread().interrupt(); // Preserve interrupt status
                break; // Exit loop
            } catch (Exception e) {
                SampleLogger.log("Exception during wait: " + e.getMessage());
                sending = false; // Stop on other exceptions too
                break;
            }
            sendOneFrame(data, nextSendTime);
            if (lastSendTime == 0) {
                lastSendTime = System.currentTimeMillis();
            }
            ++sendPiece;
            nextSendTime = lastSendTime + sendPiece * interval;
        }
    }

    // reset fos for loop reading
    protected void reset() {
        if (fos != null) {
            try {
                fos.close();
                fos = new FileInputStream(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        sending = false;
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fos = null;
        }
    }

    public abstract void sendOneFrame(byte[] data, long timestamp);

    public abstract byte[] readOneFrame(FileInputStream fos);

    public boolean isSending() {
        return sending;
    }
}
