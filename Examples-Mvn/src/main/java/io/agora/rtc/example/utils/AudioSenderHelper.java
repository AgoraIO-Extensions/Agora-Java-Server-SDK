package io.agora.rtc.example.utils;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.EncodedAudioFrameInfo;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.mediautils.OpusReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AudioSenderHelper {
    private static final int INTERVAL_ONE_FRAME = 20; // ms
    private static final int SCHEDULER_POOL_SIZE = 2;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> readFileFuture;
    private ScheduledFuture<?> sendDataFuture;
    private AudioFrameCache audioFrameCache;
    private EncodedAudioFrameInfo encodedInfo = null;

    private OpusReader opusReader;
    private final List<TaskInfo> taskList;
    private volatile TaskInfo currentTask;
    private volatile boolean isRunning = false;

    private AudioSenderCallback callback;

    public enum FileType { OPUS }

    public AudioSenderHelper() {
        this.scheduler = Executors.newScheduledThreadPool(SCHEDULER_POOL_SIZE, r -> {
            Thread thread = new Thread(r, "AudioSender-Worker");
            thread.setDaemon(true);
            return thread;
        });
        this.taskList = new CopyOnWriteArrayList<>();
    }

    public void setCallback(AudioSenderCallback callback) {
        this.callback = callback;
    }

    public int send(TaskInfo taskInfo, boolean runNow) {
        if (taskInfo == null) {
            return -1;
        }

        if (runNow) {
            cleanupAllTask();
        }
        taskList.add(taskInfo);

        if (null == readFileFuture) {
            readFileFuture = scheduler.scheduleAtFixedRate(
                this::readFile, 0, INTERVAL_ONE_FRAME, TimeUnit.MILLISECONDS);
        }

        if (sendDataFuture == null) {
            sendDataFuture = scheduler.scheduleAtFixedRate(
                this::sendData, 0, INTERVAL_ONE_FRAME, TimeUnit.MILLISECONDS);
        }

        return 0;
    }

    private void readFile() {
        try {
            if (currentTask == null) {
                if (taskList.isEmpty()) {
                    return;
                }
                currentTask = taskList.remove(0);
                if (callback != null) {
                    callback.onTaskStart(currentTask);
                }
            }

            switch (currentTask.getFileType()) {
                case OPUS:
                    if (opusReader == null) {
                        opusReader = new OpusReader(currentTask.getFilePath());
                    }
                    io.agora.rtc.example.mediautils.AudioFrame opusFrame =
                        opusReader.getAudioFrame(INTERVAL_ONE_FRAME);
                    if (opusFrame != null) {
                        if (audioFrameCache == null) {
                            audioFrameCache = new AudioFrameCache(opusFrame.numberOfChannels,
                                opusFrame.sampleRate, opusFrame.samplesPerChannel, opusFrame.codec);
                        }
                        audioFrameCache.pushFrame(new AudioFrameCache.Frame(opusFrame.buffer));
                    } else {
                        currentTask.decrementLoopCount();
                        if (currentTask.getRemainingLoops() <= 0) {
                            if (callback != null) {
                                callback.onTaskComplete(currentTask);
                            }
                            cleanupCurrentTask();
                            if (taskList.isEmpty()) {
                                if (readFileFuture != null && !readFileFuture.isCancelled()) {
                                    readFileFuture.cancel(true);
                                    readFileFuture = null;
                                }
                            } else {
                                currentTask = null;
                            }
                        } else if (currentTask.getRemainingLoops() > 0) {
                            SampleLogger.log(
                                "readFile getRemainingLoops: " + currentTask.getRemainingLoops());
                            opusReader.reset();
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            SampleLogger.log("Error in readFile: " + e.getMessage());
        }
    }

    private void sendData() {
        try {
            if (currentTask == null) {
                return;
            }

            if (audioFrameCache == null) {
                return;
            }

            AudioFrameCache.Frame[] frames = audioFrameCache.getFrames();
            if (frames == null || frames.length == 0) {
                if (readFileFuture == null) {
                    if (sendDataFuture != null && !sendDataFuture.isCancelled()) {
                        sendDataFuture.cancel(true);
                        sendDataFuture = null;
                    }
                }
                return;
            }
            switch (currentTask.getFileType()) {
                case OPUS:
                    for (AudioFrameCache.Frame frame : frames) {
                        byte[] sendData = frame.getData();
                        if (encodedInfo == null) {
                            encodedInfo = new EncodedAudioFrameInfo();
                            encodedInfo.setCodec(audioFrameCache.getCodec());
                            encodedInfo.setNumberOfChannels(audioFrameCache.getNumOfChannels());
                            encodedInfo.setSampleRateHz(audioFrameCache.getSampleRate());
                            encodedInfo.setSamplesPerChannel(
                                audioFrameCache.getSamplesPerChannel());
                        }
                        ((AgoraRtcConn) currentTask.getSender())
                            .pushAudioEncodedData(sendData, encodedInfo);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            SampleLogger.log("Error in sendData: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void cleanupCurrentTask() {
        if (opusReader != null) {
            opusReader.close();
            opusReader = null;
            encodedInfo = null;
        }
    }

    public void cleanupAllTask() {
        cleanupCurrentTask();

        if (audioFrameCache != null) {
            audioFrameCache.clear();
            audioFrameCache = null;
        }

        if (callback != null) {
            if (null != currentTask) {
                callback.onTaskCancel(currentTask);
            }
            for (TaskInfo task : taskList) {
                callback.onTaskCancel(task);
            }
        }

        taskList.clear();
        currentTask = null;
    }

    public List<TaskInfo> getAllTasks() {
        return taskList;
    }

    public TaskInfo getCurrentTask() {
        return currentTask;
    }

    public void destroy() {
        try {
            if (readFileFuture != null) {
                readFileFuture.cancel(true);
                readFileFuture = null;
            }
            if (sendDataFuture != null) {
                sendDataFuture.cancel(true);
                sendDataFuture = null;
            }
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
                try {
                    scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (opusReader != null) {
                try {
                    opusReader.close();
                } finally {
                    opusReader = null;
                }
            }
            if (audioFrameCache != null) {
                audioFrameCache.clear();
                audioFrameCache = null;
            }

            taskList.clear();
            currentTask = null;
        } catch (Exception e) {
            SampleLogger.log("Error during destroy: " + e.getMessage());
        }
    }

    public static class TaskInfo {
        private final String channelId;
        private final String userId;
        private final String filePath;
        private final FileType fileType;
        private final Object sender;
        private final int loopCount;
        private volatile int remainingLoops;

        public TaskInfo(String channelId, String userId, String filePath, FileType fileType,
            Object sender, int loopCount) {
            this.channelId = channelId;
            this.userId = userId;
            this.filePath = filePath;
            this.fileType = fileType;
            this.sender = sender;
            this.loopCount = loopCount;
            this.remainingLoops = loopCount;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getUserId() {
            return userId;
        }

        public String getFilePath() {
            return filePath;
        }

        public FileType getFileType() {
            return fileType;
        }

        public Object getSender() {
            return sender;
        }

        public int getLoopCount() {
            return loopCount;
        }

        public int getRemainingLoops() {
            return remainingLoops;
        }

        public void decrementLoopCount() {
            remainingLoops--;
        }

        @Override
        public String toString() {
            return "TaskInfo{"
                + "channelId='" + channelId + '\'' + ", userId='" + userId + '\'' + ", filePath='"
                + filePath + '\'' + ", fileType=" + fileType + ", sender=" + sender
                + ", loopCount=" + loopCount + '}';
        }
    }

    public interface AudioSenderCallback {
        void onTaskStart(TaskInfo task);

        void onTaskComplete(TaskInfo task);

        void onTaskCancel(TaskInfo task);
    }
}
