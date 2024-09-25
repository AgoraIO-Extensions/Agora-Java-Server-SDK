#pragma once
#include <stdint.h>

typedef struct _MediaFrame {
    // common fields
    int stream_index;
    // AVMEDIA_TYPE_VIDEO or AVMEDIA_TYPE_AUDIO
    int frame_type;
    // time in ms
    int64_t pts;
    uint8_t *buffer;
    int buffer_size;
    // video pixel format or audio sample format
    int format;

    // video fields
    int width;
    int height;
    int stride;
    int fps; // 新增字段：视频帧率

    // audio fields
    int samples;
    int channels;
    int sample_rate;
    int bytes_per_sample;
} MediaFrame;

// return decoder handle
extern void *open_media_file(const char *file_name);
extern int64_t get_media_duration(void *decoder);
extern int get_frame(void *decoder, MediaFrame *frame);
void close_media_file(void *decoder);