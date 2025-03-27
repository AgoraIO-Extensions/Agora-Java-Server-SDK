//
// Created by CQ on 2022/6/9.
//
#include "helper_vp8_parser.h"

#include "helper_h264_parser.h"

#include <fcntl.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>

#include "log.h"

bool HelperVp8FileParser::IsKeyFrame(const uint8_t * buf ){
    int tmp = (buf[2] << 16) | (buf[1]<< 8) |buf[0];
    int key_frame = tmp & 0x1;
    return !key_frame;
}


HelperVp8FileParser::HelperVp8FileParser(const char *filepath)
        : file_path_(filepath), data_buffer_(nullptr), offsetsize(0)
{
}

HelperVp8FileParser::~HelperVp8FileParser()
{
    if (data_buffer_) {
        // unmap the file
        if ((munmap((void *)data_buffer_, data_size_)) == -1) {
            perror("munmap");
        }
    }
}

bool HelperVp8FileParser::initialize()
{
    int fd;
    struct stat sb;
    void* mapped;
    if ((fd = open(file_path_.c_str(), O_RDONLY)) < 0) {
        perror(file_path_.c_str());
        return false;
    }
    AG_LOG(INFO, "Open vp8 file %s successfully", file_path_.c_str());

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
    data_buffer_ = mappedFile =  (uint8_t *)mapped;
    ivf_header_ = (IVF_HEADER*)data_buffer_;
    data_buffer_  += sizeof(IVF_HEADER);
    offsetsize = sizeof(IVF_HEADER);
    return true;
}

void HelperVp8FileParser::setFileParseRestart()
{
    data_buffer_ = mappedFile;
    ivf_header_ = (IVF_HEADER*)data_buffer_;
    data_buffer_ += sizeof(IVF_HEADER);
    offsetsize = sizeof(IVF_HEADER);
    AG_LOG(INFO, "Reset the videoFile !");
}

std::unique_ptr<HelperVideoFrame> HelperVp8FileParser::getVp8Frame()
{
    if (offsetsize >= data_size_){
        return nullptr;
    }
    IVF_PAYLOAD* payload = (IVF_PAYLOAD*)data_buffer_;
    const uint8_t* buf = (uint8_t*)data_buffer_;
    int frame_type;
    if (IsKeyFrame(buf+ sizeof(IVF_PAYLOAD))) {
        frame_type = 3;
    } else {
        frame_type = 4;
    }
    int rotation = 0;
    int codecType = 1;
    int frameType = frame_type;
    int width = ivf_header_->width;
    int height = ivf_header_->height;
    std::unique_ptr<HelperVideoFrame> h264Frame = nullptr;
    int len = static_cast<int>(payload->size);
    std::unique_ptr<uint8_t[]> buffer (new uint8_t[len]);
    memcpy(buffer.get(), data_buffer_+sizeof(IVF_PAYLOAD), len);
    h264Frame.reset(new HelperVideoFrame{width,height,codecType,rotation,frameType,len,std::move(buffer)});
    data_buffer_ += (payload->size + sizeof(IVF_PAYLOAD));
    offsetsize += payload->size + sizeof(IVF_PAYLOAD);
    return h264Frame;
}

