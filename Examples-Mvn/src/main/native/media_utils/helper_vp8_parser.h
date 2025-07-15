//
// Created by CQ on 2022/6/9.
//

#ifndef C_TEMPLATE_HELPER_VP8_PARSER_H
#define C_TEMPLATE_HELPER_VP8_PARSER_H


#include <memory>
#include <string>
#include "helper_video_frame.h"

struct IVF_PAYLOAD {
    uint32_t size;
    char timestamp[8];
    // uint32_t frame_type;
    //uint64_t timestamp;
};

struct IVF_HEADER {
    uint32_t signature;
    uint16_t version;
    uint16_t head_len;
    uint32_t codec;
    uint16_t width;
    uint16_t height;
    uint32_t time_scale;
    uint32_t frame_rate;
    uint32_t frames;
    uint32_t unused;
};

class HelperVp8FileParser {
public:
    HelperVp8FileParser(const char* filepath);
    ~HelperVp8FileParser();

    std::unique_ptr<HelperVideoFrame> getVp8Frame();
    bool initialize();
    void setFileParseRestart();

private:
    bool IsKeyFrame(const uint8_t * buf );
    std::string file_path_;
    int offsetsize = 0;
    int data_size_;
    uint8_t* data_buffer_;
    uint8_t* mappedFile;
    IVF_HEADER* ivf_header_;
};


#endif //C_TEMPLATE_HELPER_VP8_PARSER_H
