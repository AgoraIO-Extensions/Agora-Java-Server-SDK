#include "helper_h264_parser.h"

#include <fcntl.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>

#include "log.h"

void print_frame(uint8_t *buffer, int size) {
    int i = 0;
    printf("addr:%p, size:%d\n", buffer, size);
    while (i < size) {
        printf("%02X ", buffer[i++]);
    }
    printf("\n");
}

/**
 Find the beginning and end of a NAL (Network Abstraction Layer) unit in a byte
 buffer containing H264 bitstream data.
 @param[in]   buf        the buffer
 @param[in]   size       the size of the buffer
 @param[out]  nal_start  the beginning offset of the nal
 @param[out]  nal_end    the end offset of the nal
 @return                 the length of the nal, or 0 if did not find start of
 nal, or -1 if did not find end of nal
 */
// DEPRECATED - this will be replaced by a similar function with a slightly
// different API
int find_nal_unit(uint8_t *buf, int size, uint8_t &nal_type, int &nal_start, int &nal_end) {
    if (buf == NULL || size <= 0) {
        return 0;
    }

    if (size < 4) {
        return 0;
    }
    int i = 0;
    // find start
    nal_start = 0;
    nal_end = 0;

    while (i + 3 < size && (buf[i] != 0 || buf[i + 1] != 0 ||
                            !(buf[i + 2] == 1 || (buf[i + 2] == 0 && buf[i + 3] == 1)))) {
        i++;
        if (size < i + 4) {
            return 0;
        } // did not find nal start
    }

    if (i + 3 >= size) {
        return 0;
    }

    nal_start = i;

    if (buf[i + 2] == 1) {
        if (i + 3 >= size) {
            return 0;
        }
        nal_type = buf[i + 3] & 0x1f;
        i += 4;
    } else if (size > i + 4) {
        if (i + 4 >= size) {
            return 0;
        }
        nal_type = buf[i + 4] & 0x1f;
        i += 5;
    } else {
        return 0;
    }

    if (size < i + 4) {
        nal_end = size - 1;
        return -1;
    }

    while (i + 3 < size && (buf[i] != 0 || buf[i + 1] != 0 ||
                            !(buf[i + 2] == 1 || (buf[i + 2] == 0 && buf[i + 3] == 1)))) {
        i++;
        if (size < i + 4) {
            nal_end = size - 1;
            return -1;
        } // did not find nal end, stream ended first
    }

    nal_end = i - 1;
    return (nal_end - nal_start);
}

#define BIT(num, bit) (((num) & (1 << (7 - bit))) > 0)
int exp_golomb_decode(uint8_t *buffer, int size, int &bitOffset) {
    int totalBits = size << 3;
    int leadingZeroBits = 0;
    for (int i = bitOffset; i < totalBits && !BIT(buffer[i / 8], i % 8); i++) {
        leadingZeroBits++;
    }
    int offset = 0;
    int bitPos = bitOffset + leadingZeroBits + 1;
    for (int i = 0; i < leadingZeroBits; i++) {
        offset = (offset << 1) + BIT(buffer[bitPos / 8], bitPos % 8);
        bitPos++;
    }
    bitOffset += leadingZeroBits + 1 + leadingZeroBits;
    return (1 << leadingZeroBits) - 1 + offset;
}

HelperH264FileParser::HelperH264FileParser(const char *filepath)
    : file_path_(filepath), data_buffer_(nullptr), data_offset_(0) {
}

HelperH264FileParser::~HelperH264FileParser() {
    if (data_buffer_) {
        // unmap the file
        if ((munmap((void *)data_buffer_, data_size_)) == -1) {
            perror("munmap");
        }
    }
}

bool HelperH264FileParser::initialize() {
    int fd;
    struct stat sb;
    void *mapped;

    if ((fd = open(file_path_.c_str(), O_RDONLY)) < 0) {
        perror(file_path_.c_str());
        return false;
    }
    AG_LOG(INFO, "Open h264 file %s successfully", file_path_.c_str());

    // get the file property
    if ((fstat(fd, &sb)) == -1) {
        perror("fstat");
        close(fd);
        return false;
    }

    // map the file to process address space
    if ((mapped = mmap(NULL, sb.st_size, PROT_READ, MAP_PRIVATE, fd, 0)) == (void *)-1) {
        perror("mmap");
        close(fd);
        return false;
    }
    close(fd);

    data_size_ = sb.st_size;
    data_buffer_ = (uint8_t *)mapped;
    return true;
}

void HelperH264FileParser::setFileParseRestart() {
    std::lock_guard<std::mutex> lock(frame_mutex_);
    data_offset_ = 0;
}

void HelperH264FileParser::_getH264Frame(std::unique_ptr<HelperH264Frame> &h264Frame,
                                         bool is_key_frame, int frame_start, int frame_end) {
    if (frame_start < 0 || frame_end < frame_start || frame_end >= data_size_) {
        AG_LOG(ERROR, "Invalid frame boundaries: start=%d, end=%d, size=%d", frame_start, frame_end,
               data_size_);
        return;
    }

    int datalen = frame_end - frame_start + 1;
    std::unique_ptr<uint8_t[]> buffer(new uint8_t[datalen]);
    memcpy(buffer.get(), &data_buffer_[frame_start], datalen);
    h264Frame.reset(new HelperH264Frame{is_key_frame ? true : false, std::move(buffer), datalen});
    // printf("frame_type:%d\n", h264Frame->frameType);
    // print_frame(&data_buffer_[frame_start], h264Frame->bufferLen);
}

std::unique_ptr<HelperH264Frame> HelperH264FileParser::getH264Frame() {
    std::lock_guard<std::mutex> lock(frame_mutex_);

    std::unique_ptr<HelperH264Frame> h264Frame = nullptr;
    uint8_t nal_type = 0;
    int nal_start = 0;
    int nal_end = 0;
    bool is_key_frame = false, is_sps = false, is_pps = false;
    int frame_start = 0;
    int frame_end = 0;
    int ret;

    if (data_buffer_ == nullptr || data_size_ <= 0 || data_offset_ >= data_size_) {
        AG_LOG(ERROR, "Invalid data buffer state: buffer=%p, size=%d, offset=%d", data_buffer_,
               data_size_, data_offset_);
        return nullptr;
    }

    // get first nalu for frame_start
    ret = find_nal_unit(&data_buffer_[data_offset_], data_size_ - data_offset_, nal_type, nal_start,
                        nal_end);
    if (ret == 0) {
        AG_LOG(INFO, "End of video file, offset:%d, size:%d", data_offset_, data_size_);
        data_offset_ = 0;
        return h264Frame;
    }
    if (nal_type == 8) {
        is_pps = true;
    }
    if (nal_type == 7) {
        is_sps = true;
    }
    frame_start = data_offset_ + nal_start;

    // get first I slice or P slice for frame_type
    while (nal_type != 1 && nal_type != 5) {
        data_offset_ += nal_end + 1;
        ret = find_nal_unit(&data_buffer_[data_offset_], data_size_ - data_offset_, nal_type,
                            nal_start, nal_end);
        if (nal_type == 8) {
            is_pps = true;
        }
        if (nal_type == 7) {
            is_sps = true;
        }
        if (ret == 0) {
            AG_LOG(INFO, "End of video file, offset:%d, size:%d", data_offset_, data_size_);
            data_offset_ = 0;
            return h264Frame;
        }
    }
    int offset = data_offset_ + nal_start;
    offset += data_buffer_[offset + 2] ? 3 : 4 + 1;

    int bitOffset = 0;
    int first_mb_in_slice =
        exp_golomb_decode(&data_buffer_[offset], data_size_ - offset, bitOffset);
    int slice_type = exp_golomb_decode(&data_buffer_[offset], data_size_ - offset, bitOffset);

    if (nal_type == 5) { // IDR
        is_key_frame = true;
    } else {
        slice_type %= 5;
        if (slice_type == 2 || slice_type == 4) { // I SLICE
            is_key_frame = true;
        } else { // P SLICE
            is_key_frame = false;
        }
    }
    int prev_first_mb_in_slice = first_mb_in_slice;
    int prev_nal_type = nal_type;

    // judge the slice is the last slice in a frame or not
    while (true) {
        data_offset_ += nal_end + 1;
        ret = find_nal_unit(&data_buffer_[data_offset_], data_size_ - data_offset_, nal_type,
                            nal_start, nal_end);
        if (prev_nal_type != nal_type)
            break;
        offset = data_offset_ + nal_start;
        offset += data_buffer_[offset + 2] ? 3 : 4 + 1;
        bitOffset = 0;
        int Last_FNIS = first_mb_in_slice;
        first_mb_in_slice =
            exp_golomb_decode(&data_buffer_[offset], data_size_ - offset, bitOffset);
        if ((prev_first_mb_in_slice > first_mb_in_slice) ||
            (prev_first_mb_in_slice == first_mb_in_slice && prev_first_mb_in_slice == 0)) {
            break;
        }
        if (ret == 0) {
            AG_LOG(INFO, "End of video file, offset:%d, size:%d", data_offset_, data_size_);
            // printf("num_I is %d,num_P is %d",num_I,num_P);
            _getH264Frame(h264Frame, is_key_frame && is_pps && is_sps, frame_start, frame_end);
            data_offset_ = 0;
            return h264Frame;
        }
    }

    frame_end = data_offset_ - 1;
    // frame_end = data_offset_ + nal_end;
    // data_offset_ += nal_end + 1;
    _getH264Frame(h264Frame, is_key_frame && is_pps && is_sps, frame_start, frame_end);

    return h264Frame;
}
