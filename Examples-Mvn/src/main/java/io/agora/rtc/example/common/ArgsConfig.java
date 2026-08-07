package io.agora.rtc.example.common;

public class ArgsConfig implements Cloneable {
    private String appId = "";
    private String token = "";

    private String userId = "";
    private String channelId = "";
    private String remoteUserId = "";
    private int audioScenario = -1;
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
    private boolean isBroadcaster = false;
    private boolean isRecvAudioEncodedFrame = false;
    private boolean enableAssistantDevice = false;
    private boolean isAutoTest = false;
    private boolean enableApmAndDump = false;

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

    public int getAudioScenario() {
        return audioScenario;
    }

    public void setAudioScenario(int audioScenario) {
        this.audioScenario = audioScenario;
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

    public boolean isBroadcaster() {
        return isBroadcaster;
    }

    public void setBroadcaster(boolean broadcaster) {
        this.isBroadcaster = broadcaster;
    }

    public boolean isRecvAudioEncodedFrame() {
        return isRecvAudioEncodedFrame;
    }

    public void setRecvAudioEncodedFrame(boolean isRecvAudioEncodedFrame) {
        this.isRecvAudioEncodedFrame = isRecvAudioEncodedFrame;
    }

    public boolean isEnableAssistantDevice() {
        return enableAssistantDevice;
    }

    public void setEnableAssistantDevice(boolean enableAssistantDevice) {
        this.enableAssistantDevice = enableAssistantDevice;
    }

    public boolean isAutoTest() {
        return isAutoTest;
    }

    public void setAutoTest(boolean isAutoTest) {
        this.isAutoTest = isAutoTest;
    }

    public boolean isEnableApmAndDump() {
        return enableApmAndDump;
    }

    public void setEnableApmAndDump(boolean enableApmAndDump) {
        this.enableApmAndDump = enableApmAndDump;
    }

    @Override
    public ArgsConfig clone() {
        try {
            return (ArgsConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }

    /**
     * create a deep copy of ArgsConfig
     * 
     * @return deep copy of ArgsConfig
     */
    public ArgsConfig deepClone() {
        ArgsConfig cloned = new ArgsConfig();

        // Copy all fields
        cloned.appId = this.appId;
        cloned.token = this.token;
        cloned.userId = this.userId;
        cloned.channelId = this.channelId;
        cloned.remoteUserId = this.remoteUserId;
        cloned.audioScenario = this.audioScenario;
        cloned.streamType = this.streamType;
        cloned.audioFile = this.audioFile;
        cloned.audioOutFile = this.audioOutFile;
        cloned.videoFile = this.videoFile;
        cloned.lowVideoFile = this.lowVideoFile;
        cloned.videoOutFile = this.videoOutFile;
        cloned.expectedFile = this.expectedFile;
        cloned.sampleRate = this.sampleRate;
        cloned.numOfChannels = this.numOfChannels;
        cloned.height = this.height;
        cloned.width = this.width;
        cloned.lowWidth = this.lowWidth;
        cloned.lowHeight = this.lowHeight;
        cloned.fps = this.fps;
        cloned.lowFps = this.lowFps;
        cloned.connectionCount = this.connectionCount;
        cloned.fileType = this.fileType;
        cloned.encryptionMode = this.encryptionMode;
        cloned.encryptionKey = this.encryptionKey;
        cloned.enableStringUid = this.enableStringUid;
        cloned.enableLog = this.enableLog;
        cloned.enableEncryptionMode = this.enableEncryptionMode;
        cloned.enableCloudProxy = this.enableCloudProxy;
        cloned.enableSimulcastStream = this.enableSimulcastStream;
        cloned.enableSaveFile = this.enableSaveFile;
        cloned.enableAlpha = this.enableAlpha;
        cloned.enableVad = this.enableVad;
        cloned.enableSendAudioMetaData = this.enableSendAudioMetaData;
        cloned.enableSendVideoMetaData = this.enableSendVideoMetaData;
        cloned.enableSingleChannel = this.enableSingleChannel;
        cloned.enableStressTest = this.enableStressTest;
        cloned.enableRecvDataStream = this.enableRecvDataStream;
        cloned.logFilter = this.logFilter;
        cloned.testTime = this.testTime;
        cloned.sleepTime = this.sleepTime;
        cloned.timeForStressLeave = this.timeForStressLeave;
        cloned.isBroadcaster = this.isBroadcaster;
        cloned.isRecvAudioEncodedFrame = this.isRecvAudioEncodedFrame;
        cloned.enableAssistantDevice = this.enableAssistantDevice;
        cloned.isAutoTest = this.isAutoTest;
        cloned.enableApmAndDump = this.enableApmAndDump;
        return cloned;
    }

    @Override
    public String toString() {
        return "ArgsConfig{"
                + "appId='" + appId + '\'' + ", token='" + token + '\'' + ", userId='" + userId + '\''
                + ", channelId='" + channelId + '\'' + ", remoteUserId='" + remoteUserId + '\''
                + ", audioScenario='" + audioScenario + '\'' + ", streamType='" + streamType + '\''
                + ", audioFile='" + audioFile + '\'' + ", audioOutFile='" + audioOutFile + '\''
                + ", videoFile='" + videoFile + '\'' + ", lowVideoFile='" + lowVideoFile + '\''
                + ", videoOutFile='" + videoOutFile + '\'' + ", expectedFile='" + expectedFile + '\''
                + ", sampleRate='" + sampleRate + '\'' + ", numOfChannels='" + numOfChannels + '\''
                + ", height='" + height + '\'' + ", width='" + width + '\'' + ", lowWidth='"
                + lowWidth + '\'' + ", lowHeight='" + lowHeight + '\'' + ", fps='" + fps + '\''
                + ", lowFps='" + lowFps + '\'' + ", connectionCount='" + connectionCount + '\''
                + ", fileType='" + fileType + '\'' + ", encryptionMode='" + encryptionMode + '\''
                + ", encryptionKey='" + encryptionKey + '\'' + ", enableStringUid='" + enableStringUid
                + '\'' + ", enableLog='" + enableLog + '\'' + ", enableEncryptionMode='"
                + enableEncryptionMode + '\'' + ", enableCloudProxy='" + enableCloudProxy + '\''
                + ", enableSimulcastStream='" + enableSimulcastStream + '\'' + ", enableSaveFile='"
                + enableSaveFile + '\'' + ", enableAlpha='" + enableAlpha + '\'' + ", enableVad='"
                + enableVad + '\'' + ", enableSendAudioMetaData='" + enableSendAudioMetaData + '\''
                + ", enableSendVideoMetaData='" + enableSendVideoMetaData + '\''
                + ", enableSingleChannel='" + enableSingleChannel + '\'' + ", enableStressTest='"
                + enableStressTest + '\'' + ", enableRecvDataStream='" + enableRecvDataStream + '\''
                + ", logFilter='" + logFilter + '\'' + ", testTime='" + testTime + '\''
                + ", sleepTime='" + sleepTime + '\'' + ", timeForStressLeave='" + timeForStressLeave
                + '\'' + ", isBroadcaster='" + isBroadcaster + '\'' + ", isRecvAudioEncodedFrame='"
                + isRecvAudioEncodedFrame + '\'' + ", enableAssistantDevice='" + enableAssistantDevice
                + '\'' + ", isAutoTest='" + isAutoTest + '\'' + ", enableApmAndDump='" + enableApmAndDump
                + '\'' + '}';
    }
}
