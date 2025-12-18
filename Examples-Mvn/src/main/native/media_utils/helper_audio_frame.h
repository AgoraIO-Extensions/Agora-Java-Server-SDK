#include <cstdint>
#ifndef HELPAUDIOFRAME
#define HELPAUDIOFRAME

struct HelperAudioFrame {
    //  agora::rtc::EncodedAudioFrameInfo audioFrameInfo;
    int numberOfChannels;
    int sampleRate;
    int codec;
    int samplesPerChannel;
    uint8_t *buffer;
    int bufferLen;
    bool ownsBuffer;  // true if buffer was allocated with new[] and should be freed

    // Constructor for owned buffer (allocated with new[])
    HelperAudioFrame(int channels, int rate, int codecType, int samples, uint8_t *buf, int len,
                     bool owns = true)
        : numberOfChannels(channels),
          sampleRate(rate),
          codec(codecType),
          samplesPerChannel(samples),
          buffer(buf),
          bufferLen(len),
          ownsBuffer(owns) {
    }

    // Destructor to properly clean up the buffer if owned
    ~HelperAudioFrame() {
        if (buffer != nullptr && ownsBuffer) {
            delete[] buffer;
            buffer = nullptr;
        }
    }
};
#endif