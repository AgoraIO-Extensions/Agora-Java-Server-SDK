//
// Created by CQ on 2022/6/9.
//

#ifndef C_TEMPLATE_HELPER_VIDEO_FRAME_H
#define C_TEMPLATE_HELPER_VIDEO_FRAME_H

struct HelperVideoFrame {
    int width;
    int height;
    int codec;
    int rotation;
    int frametype;
    int bufferLen;
    std::unique_ptr<uint8_t[]> buffer;
};
#endif //C_TEMPLATE_HELPER_VIDEO_FRAME_H
