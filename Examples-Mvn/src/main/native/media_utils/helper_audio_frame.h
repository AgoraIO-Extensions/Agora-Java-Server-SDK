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
};
#endif