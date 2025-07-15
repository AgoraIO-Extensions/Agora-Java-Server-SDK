package io.agora.rtc.example.common;

public class ArgsConfig {
    private String appId = "";
    private String token = "";

    private String userId = "";
    private String channelId = "";
    private String remoteUserId = "";
    private String streamType = "";
    private String audioFile = "";
    private String audioOutFile = "";
    private String videoFile = "";
    private String lowVideoFile = "";
    private String videoOutFile = "";
    private String expectedFile = "";
    private int sampleRate = 0;
    private int numOfChannels = 0;
    private int height = 0;
    private int width = 0;
    private int lowWidth = 0;
    private int lowHeight = 0;
    private int fps = 0;
    private int lowFps = 0;
    private int connectionCount = 0;
    private String fileType = "";
    private int encryptionMode = 0;
    private String encryptionKey = "";
    private boolean enableStringUid = false;
    private boolean enableLog = false;
    private boolean enableEncryptionMode = false;
    private boolean enableCloudProxy = false;
    private boolean enableSimulcastStream = false;
    private boolean enableSaveFile = false;
    private boolean enableAlpha = false;
    private boolean enableVad = false;
    private boolean enableSendAudioMetaData = false;
    private boolean enableSendVideoMetaData = false;
    private boolean enableSingleChannel = false;
    private boolean enableStressTest = false;
    private boolean enableRecvDataStream = false;
    private int logFilter = 0;
    private int testTime = 0;
    private float sleepTime = 0;
    private int timeForStressLeave = 0;
    private boolean isSender = false;
    private boolean isRecvAudioEncodedFrame = false;

    // Getter and Setter methods
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getRemoteUserId() {
        return remoteUserId;
    }

    public void setRemoteUserId(String remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getAudioOutFile() {
        return audioOutFile;
    }

    public void setAudioOutFile(String audioOutFile) {
        this.audioOutFile = audioOutFile;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    public String getLowVideoFile() {
        return lowVideoFile;
    }

    public void setLowVideoFile(String lowVideoFile) {
        this.lowVideoFile = lowVideoFile;
    }

    public String getVideoOutFile() {
        return videoOutFile;
    }

    public void setVideoOutFile(String videoOutFile) {
        this.videoOutFile = videoOutFile;
    }

    public String getExpectedFile() {
        return expectedFile;
    }

    public void setExpectedFile(String expectedFile) {
        this.expectedFile = expectedFile;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getNumOfChannels() {
        return numOfChannels;
    }

    public void setNumOfChannels(int numOfChannels) {
        this.numOfChannels = numOfChannels;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLowWidth() {
        return lowWidth;
    }

    public void setLowWidth(int lowWidth) {
        this.lowWidth = lowWidth;
    }

    public int getLowHeight() {
        return lowHeight;
    }

    public void setLowHeight(int lowHeight) {
        this.lowHeight = lowHeight;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getLowFps() {
        return lowFps;
    }

    public void setLowFps(int lowFps) {
        this.lowFps = lowFps;
    }

    public boolean isEnableStringUid() {
        return enableStringUid;
    }

    public void setEnableStringUid(boolean enableStringUid) {
        this.enableStringUid = enableStringUid;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public float getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(float sleepTime) {
        this.sleepTime = sleepTime;
    }

    public boolean isEnableLog() {
        return enableLog;
    }

    public void setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }

    public boolean isEnableEncryptionMode() {
        return enableEncryptionMode;
    }

    public void setEnableEncryptionMode(boolean enableEncryptionMode) {
        this.enableEncryptionMode = enableEncryptionMode;
    }

    public int getEncryptionMode() {
        return encryptionMode;
    }

    public void setEncryptionMode(int encryptionMode) {
        this.encryptionMode = encryptionMode;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isEnableCloudProxy() {
        return enableCloudProxy;
    }

    public void setEnableCloudProxy(boolean enableCloudProxy) {
        this.enableCloudProxy = enableCloudProxy;
    }

    public boolean isEnableSimulcastStream() {
        return enableSimulcastStream;
    }

    public void setEnableSimulcastStream(boolean enableSimulcastStream) {
        this.enableSimulcastStream = enableSimulcastStream;
    }

    public boolean isEnableSaveFile() {
        return enableSaveFile;
    }

    public void setEnableSaveFile(boolean enableSaveFile) {
        this.enableSaveFile = enableSaveFile;
    }

    public boolean isEnableAlpha() {
        return enableAlpha;
    }

    public void setEnableAlpha(boolean enableAlpha) {
        this.enableAlpha = enableAlpha;
    }

    public boolean isEnableVad() {
        return enableVad;
    }

    public void setEnableVad(boolean enableVad) {
        this.enableVad = enableVad;
    }

    public boolean isEnableSendAudioMetaData() {
        return enableSendAudioMetaData;
    }

    public void setEnableSendAudioMetaData(boolean enableSendAudioMetaData) {
        this.enableSendAudioMetaData = enableSendAudioMetaData;
    }

    public boolean isEnableSendVideoMetaData() {
        return enableSendVideoMetaData;
    }

    public void setEnableSendVideoMetaData(boolean enableSendVideoMetaData) {
        this.enableSendVideoMetaData = enableSendVideoMetaData;
    }

    public boolean isEnableSingleChannel() {
        return enableSingleChannel;
    }

    public void setEnableSingleChannel(boolean enableSingleChannel) {
        this.enableSingleChannel = enableSingleChannel;
    }

    public boolean isEnableStressTest() {
        return enableStressTest;
    }

    public void setEnableStressTest(boolean enableStressTest) {
        this.enableStressTest = enableStressTest;
    }

    public boolean isEnableRecvDataStream() {
        return enableRecvDataStream;
    }

    public void setEnableRecvDataStream(boolean enableRecvDataStream) {
        this.enableRecvDataStream = enableRecvDataStream;
    }

    public int getTimeForStressLeave() {
        return timeForStressLeave;
    }

    public void setTimeForStressLeave(int timeForStressLeave) {
        this.timeForStressLeave = timeForStressLeave;
    }

    public int getLogFilter() {
        return logFilter;
    }

    public void setLogFilter(int logFilter) {
        this.logFilter = logFilter;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        this.isSender = sender;
    }

    public boolean isRecvAudioEncodedFrame() {
        return isRecvAudioEncodedFrame;
    }

    public void setRecvAudioEncodedFrame(boolean recvAudioEncodedFrame) {
        this.isRecvAudioEncodedFrame = recvAudioEncodedFrame;
    }

    @Override
    public String toString() {
        return "ArgsConfig{"
            + "appId='" + appId + '\'' + ", token='" + token + '\'' + ", userId='" + userId + '\''
            + ", channelId='" + channelId + '\'' + ", remoteUserId='" + remoteUserId + '\''
            + ", streamType='" + streamType + '\'' + ", audioFile='" + audioFile + '\''
            + ", audioOutFile='" + audioOutFile + '\'' + ", videoFile='" + videoFile + '\''
            + ", lowVideoFile='" + lowVideoFile + '\'' + ", videoOutFile='" + videoOutFile + '\''
            + ", expectedFile='" + expectedFile + '\'' + ", sampleRate='" + sampleRate + '\''
            + ", numOfChannels='" + numOfChannels + '\'' + ", height='" + height + '\''
            + ", width='" + width + '\'' + ", lowWidth='" + lowWidth + '\'' + ", lowHeight='"
            + lowHeight + '\'' + ", fps='" + fps + '\'' + ", lowFps='" + lowFps + '\''
            + ", connectionCount='" + connectionCount + '\'' + ", fileType='" + fileType + '\''
            + ", encryptionMode='" + encryptionMode + '\'' + ", encryptionKey='" + encryptionKey
            + '\'' + ", enableStringUid='" + enableStringUid + '\'' + ", enableLog='" + enableLog
            + '\'' + ", enableEncryptionMode='" + enableEncryptionMode + '\''
            + ", enableCloudProxy='" + enableCloudProxy + '\'' + ", enableSimulcastStream='"
            + enableSimulcastStream + '\'' + ", enableSaveFile='" + enableSaveFile + '\''
            + ", enableAlpha='" + enableAlpha + '\'' + ", enableVad='" + enableVad + '\''
            + ", enableSendAudioMetaData='" + enableSendAudioMetaData + '\''
            + ", enableSendVideoMetaData='" + enableSendVideoMetaData + '\''
            + ", enableSingleChannel='" + enableSingleChannel + '\'' + ", enableStressTest='"
            + enableStressTest + '\'' + ", enableRecvDataStream='" + enableRecvDataStream + '\''
            + ", logFilter='" + logFilter + '\'' + ", testTime='" + testTime + '\''
            + ", sleepTime='" + sleepTime + '\'' + ", timeForStressLeave='" + timeForStressLeave
            + '\'' + ", isSender='" + isSender + '\'' + ", isRecvAudioEncodedFrame='"
            + isRecvAudioEncodedFrame + '\'' + '}';
    }
}
