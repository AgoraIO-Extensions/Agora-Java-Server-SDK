#include "helper_h265_parser.h"

#include <fcntl.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>

#include "log.h"

static void print_frame(uint8_t* buffer, int size) {
    int i = 0;
    printf("addr:%p, size:%d\n", buffer, size);
    while (i < size) {
        printf("%02X ", buffer[i++]);
    }
    printf("\n");
}

/**
 Find the beginning and end of a NAL (Network Abstraction Layer) unit in a byte
 buffer containing H265 bitstream data.
 @param[in]   buf        the buffer
 @param[in]   size       the size of the buffer
 @param[out]  nal_start  the beginning offset of the nal
 @param[out]  nal_end    the end offset of the nal
 @return                 the length of the nal, or 0 if did not find start of
 nal, or -1 if did not find end of nal
 */
// DEPRECATED - this will be replaced by a similar function with a slightly
// different API
static int find_nal_unit(uint8_t* buf, int size, uint8_t& nal_type, int& nal_start, int& nal_end) {
    if (size < 4) {
        return 0;
    }
    int i = 0;
    // find start
    nal_start = 0;
    nal_end = 0;

    while (buf[i] != 0 || buf[i + 1] != 0 ||
           !(buf[i + 2] == 1 || (buf[i + 2] == 0 && buf[i + 3] == 1))) {
        i++;
        if (size < i + 4) {
            return 0;
        }  // did not find nal start
    }

    nal_start = i;

    if (buf[i + 2] == 1) {
        nal_type = (buf[i + 3] & 0x7e) >> 1;
        i += 4;
    } else if (size > i + 4) {
        nal_type = (buf[i + 4] & 0x7e) >> 1;
        i += 5;
    } else {
        return 0;
    }
    if (size < i + 4) {
        nal_end = size - 1;
        return -1;
    }

    while (buf[i] != 0 || buf[i + 1] != 0 ||
           !(buf[i + 2] == 1 || (buf[i + 2] == 0 && buf[i + 3] == 1))) {
        i++;
        if (size < i + 4) {
            nal_end = size - 1;
            return -1;
        }  // did not find nal end, stream ended first
    }

    nal_end = i - 1;
    return (nal_end - nal_start);
}

#define BIT(num, bit) (((num) & (1 << (7 - bit))) > 0)
static int exp_golomb_decode(uint8_t* buffer, int size, int& bitOffset) {
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
#undef BIT

HelperH265FileParser::HelperH265FileParser(const char* filepath)
    : file_path_(filepath), data_buffer_(nullptr), data_offset_(0), data_size_(0) {
}

HelperH265FileParser::~HelperH265FileParser() {
    if (data_buffer_) {
        // unmap the file
        if ((munmap((void*)data_buffer_, data_size_)) == -1) {
            perror("munmap");
        }
    }
}

bool HelperH265FileParser::initialize() {
    int fd;
    struct stat sb;
    void* mapped;

    if ((fd = open(file_path_.c_str(), O_RDONLY)) < 0) {
        perror(file_path_.c_str());
        return false;
    }
    AG_LOG(INFO, "Open h265 file %s successfully", file_path_.c_str());

    // get the file property
    if ((fstat(fd, &sb)) == -1) {
        perror("fstat");
        close(fd);
        return false;
    }

    // map the file to process address space
    if ((mapped = mmap(NULL, sb.st_size, PROT_READ, MAP_PRIVATE, fd, 0)) == (void*)-1) {
        perror("mmap");
        close(fd);
        return false;
    }
    close(fd);

    data_size_ = sb.st_size;
    data_buffer_ = (uint8_t*)mapped;
    return true;
}

void HelperH265FileParser::setFileParseRestart() {
    data_offset_ = 0;
}

void HelperH265FileParser::_getH265Frame(std::unique_ptr<HelperH265Frame>& h265Frame,
                                         bool is_key_frame, int frame_start, int frame_end) {
    int datalen = frame_end - frame_start + 1;
    std::unique_ptr<uint8_t[]> buffer(new uint8_t[datalen]);
    memcpy(buffer.get(), &data_buffer_[frame_start], datalen);
    h265Frame.reset(new HelperH265Frame{is_key_frame ? true : false, std::move(buffer), datalen});
    // printf("frame_type:%d\n", h265Frame->frameType);
    // print_frame(&data_buffer_[frame_start], h265Frame->bufferLen);
}

std::unique_ptr<HelperH265Frame> HelperH265FileParser::getH265Frame() {
    std::unique_ptr<HelperH265Frame> h265Frame = nullptr;
    uint8_t nal_type = 0;
    int nal_start = 0;
    int nal_end = 0;
    bool is_key_frame;
    int frame_start = 0;
    int frame_end = 0;
    int ret;

    // get first nalu for frame_start
    ret = find_nal_unit(&data_buffer_[data_offset_], data_size_ - data_offset_, nal_type, nal_start,
                        nal_end);
    if (ret == 0) {
        AG_LOG(INFO, "End of video file, offset:%d, size:%d", data_offset_, data_size_);
        data_offset_ = 0;
        return h265Frame;
    }
    frame_start = data_offset_ + nal_start;

    // get first I slice or P slice for frame_type
    while (nal_type != 1 && nal_type != 19) {
        data_offset_ += nal_end + 1;
        ret = find_nal_unit(&data_buffer_[data_offset_], data_size_ - data_offset_, nal_type,
                            nal_start, nal_end);
        if (ret == 0) {
            AG_LOG(INFO, "End of video file, offset:%d, size:%d", data_offset_, data_size_);
            data_offset_ = 0;
            return h265Frame;
        }
    }
    int offset = data_offset_ + nal_start;
    offset += data_buffer_[offset + 2] ? 3 : 4 + 1;

    int bitOffset = 0;
    int first_mb_in_slice =
        exp_golomb_decode(&data_buffer_[offset], data_size_ - offset, bitOffset);
    int slice_type = exp_golomb_decode(&data_buffer_[offset], data_size_ - offset, bitOffset);

    if (nal_type == 19) {  // IDR
        is_key_frame = true;
    } else {
        is_key_frame = false;
    }
    int prev_first_mb_in_slice = first_mb_in_slice;
    int prev_nal_type = nal_type;

    // maybe we should judge the slice is the last slice in a frame or not
    frame_end = data_offset_ - 1;
    frame_end = data_offset_ + nal_end;
    data_offset_ += nal_end + 1;
    _getH265Frame(h265Frame, is_key_frame, frame_start, frame_end);

    return h265Frame;
}
