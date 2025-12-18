#include "helper_opus_parser.h"

#include <opusfile.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <cstring>
#include <vector>

#include "log.h"

struct decode_context {
    int current_packet{0};
    int total_length{0};

    int nsamples{0};
    int nchannels;
    int format;

    unsigned char packet[960 * 2 * 2]{0};
    long bytes{0};
    long b_o_s{0};
    long e_o_s{0};

    ogg_int64_t granulepos;
    ogg_int64_t packetno;
};

class OggOpusFileParser {
   public:
    explicit OggOpusFileParser(const char *filepath);
    virtual ~OggOpusFileParser();

   public:
    // AudioFileParser
    bool open();
    bool hasNext();

    void getNext(char *buffer, int *length);

    int getCodecType();
    int getSampleRateHz();
    int getNumberOfChannels();
    int reset();

    const std::vector<uint8_t> &getOggSHeader() const {
        return oggSHeader_;
    }
    const std::vector<uint8_t> &getOpusHeader() const {
        return opusHeader_;
    }
    const std::vector<uint8_t> &getOggOpusTagsHeader() const {
        return oggOpusTagsHeader_;
    }
    const std::vector<uint8_t> &getOpusComments() const {
        return opusComments_;
    }
    const std::vector<uint8_t> &getOggAudioHeader() const {
        return oggAudioHeader_;
    }

   private:
    void loadMetaInfo(OggOpusFile *oggOpusFile);
    void captureHeaderAndComments();
    void captureOggHeaders();

   private:
    char *oggOpusFilePath_;
    OggOpusFile *oggOpusFile_;
    int sampleRateHz_;
    int numberOfChannels_;
    decode_context decode_context_;
    bool eof;
    std::vector<uint8_t> opusHeader_;
    std::vector<uint8_t> opusComments_;
    std::vector<uint8_t> oggHeader_;

    std::vector<uint8_t> oggSHeader_;
    std::vector<uint8_t> oggOpusTagsHeader_;
    std::vector<uint8_t> oggAudioHeader_;
};

int op_decode_cb(void *ctx, OpusMSDecoder *decoder, void *pcm, const ogg_packet *op, int nsamples,
                 int nchannels, int format, int li) {
    struct decode_context *context = (struct decode_context *)ctx;
    context->nsamples = nsamples;
    context->nchannels = nchannels;
    context->format = format;

    context->b_o_s = op->b_o_s;
    context->e_o_s = op->e_o_s;
    context->bytes = op->bytes;
    context->granulepos = op->granulepos;
    context->packetno = op->packetno;
    memcpy(context->packet, op->packet, op->bytes);

    context->total_length += op->bytes;
    ++context->current_packet;

    (void)pcm;
    (void)decoder;
    (void)li;

    return 0;
}

OggOpusFileParser::OggOpusFileParser(const char *filepath)
    : oggOpusFilePath_(strdup(filepath)),
      oggOpusFile_(nullptr),
      sampleRateHz_(48000),
      numberOfChannels_(0),
      eof(false) {
}

OggOpusFileParser::~OggOpusFileParser() {
    if (oggOpusFile_) {
        op_free(oggOpusFile_);
        oggOpusFile_ = nullptr;
    }
    if (oggOpusFilePath_) {
        free(static_cast<void *>(oggOpusFilePath_));
        oggOpusFilePath_ = nullptr;
    }
}

void OggOpusFileParser::loadMetaInfo(OggOpusFile *oggOpusFile) {
    const OpusHead *head = op_head(oggOpusFile, -1);
    numberOfChannels_ = head->channel_count;
    sampleRateHz_ = head->input_sample_rate;
    if (op_seekable(oggOpusFile)) {
    }
}

void OggOpusFileParser::captureOggHeaders() {
    if (!oggOpusFile_)
        return;

    op_raw_seek(oggOpusFile_, 0);

    ogg_sync_state oy;
    ogg_stream_state os;
    ogg_page og;
    ogg_packet op;

    ogg_sync_init(&oy);

    FILE *file = fopen(oggOpusFilePath_, "rb");
    if (!file)
        return;

    int pageCount = 0;
    while (true) {
        char *buffer = ogg_sync_buffer(&oy, 4096);
        int bytes = fread(buffer, 1, 4096, file);
        if (bytes == 0 && feof(file))
            break;
        ogg_sync_wrote(&oy, bytes);

        while (ogg_sync_pageout(&oy, &og) > 0) {
            if (pageCount == 0) {
                ogg_stream_init(&os, ogg_page_serialno(&og));
                oggSHeader_.assign(og.header, og.header + og.header_len);
            } else if (pageCount == 1) {
                oggOpusTagsHeader_.assign(og.header, og.header + og.header_len);
            } else if (pageCount == 2) {
                oggAudioHeader_.assign(og.header, og.header + og.header_len);
                fclose(file);
                ogg_stream_clear(&os);
                ogg_sync_clear(&oy);
                return;
            }
            ogg_stream_pagein(&os, &og);
            pageCount++;
        }
    }

    fclose(file);
    ogg_stream_clear(&os);
    ogg_sync_clear(&oy);
}
void OggOpusFileParser::captureHeaderAndComments() {
    if (!oggOpusFile_)
        return;

    // 捕获文件开头的数据
    if (op_raw_seek(oggOpusFile_, 0) != 0) {
        return;
    }

    // 读取文件开头的 1024 字节（这个大小可以根据需要调整）
    const int bufferSize = 1024;
    oggHeader_.resize(bufferSize * 2);
    int bytesRead = op_read(oggOpusFile_, reinterpret_cast<opus_int16 *>(oggHeader_.data()),
                            bufferSize, nullptr);

    if (bytesRead < 0) {
        return;
    }

    oggHeader_.resize(bytesRead * 2);  // op_read 返回的是采样数，每个采样 2 字节

    const OpusHead *head = op_head(oggOpusFile_, -1);
    if (!head) {
        return;
    }

    opusHeader_.resize(19);  // Opus 头部固定为 19 字节
    uint8_t *headPtr = opusHeader_.data();

    memcpy(headPtr, "OpusHead", 8);
    headPtr += 8;
    *headPtr++ = 1;  // 版本
    *headPtr++ = head->channel_count;
    *reinterpret_cast<uint16_t *>(headPtr) = head->pre_skip;
    headPtr += 2;
    *reinterpret_cast<uint32_t *>(headPtr) = head->input_sample_rate;
    headPtr += 4;
    *reinterpret_cast<uint16_t *>(headPtr) = head->output_gain;
    headPtr += 2;
    *headPtr = head->mapping_family;

    // 捕获 Opus 注释
    const OpusTags *tags = op_tags(oggOpusFile_, -1);
    if (!tags) {
        return;
    }

    size_t commentsSize = 8;  // "OpusTags"

    // 计算 vendor 字符串长度
    int vendor_length = tags->vendor ? strlen(tags->vendor) : 0;
    commentsSize += 4 + vendor_length;  // vendor string

    commentsSize += 4;  // user comment list length
    for (int i = 0; i < tags->comments; i++) {
        commentsSize += 4 + tags->comment_lengths[i];
    }

    opusComments_.resize(commentsSize);
    uint8_t *ptr = opusComments_.data();

    memcpy(ptr, "OpusTags", 8);
    ptr += 8;

    *reinterpret_cast<uint32_t *>(ptr) = vendor_length;
    ptr += 4;
    if (tags->vendor && vendor_length > 0) {
        memcpy(ptr, tags->vendor, vendor_length);
        ptr += vendor_length;
    }

    *reinterpret_cast<uint32_t *>(ptr) = tags->comments;
    ptr += 4;
    for (int i = 0; i < tags->comments; i++) {
        *reinterpret_cast<uint32_t *>(ptr) = tags->comment_lengths[i];
        ptr += 4;
        memcpy(ptr, tags->user_comments[i], tags->comment_lengths[i]);
        ptr += tags->comment_lengths[i];
    }
}

bool OggOpusFileParser::open() {
    int ret = 0;
    oggOpusFile_ = op_open_file(oggOpusFilePath_, &ret);
    if (ret != 0 || !oggOpusFile_) {
        return false;
    }

    op_set_decode_callback(oggOpusFile_, op_decode_cb, &decode_context_);
    loadMetaInfo(oggOpusFile_);
    captureHeaderAndComments();
    captureOggHeaders();
    return true;
}

int OggOpusFileParser::reset() {
    int ret = 0;
    if (oggOpusFile_) {
        ret = op_pcm_seek(oggOpusFile_, 0);
        eof = false;
    }
    return ret;
}

bool OggOpusFileParser::hasNext() {
    opus_int16 pcm[120 * 48 * 2];
    int ret = op_read_stereo(oggOpusFile_, pcm, sizeof(pcm) / sizeof(*pcm));
    if (ret < 0) {
        eof = true;
    }
    return ret >= 0 && !eof;
}

void OggOpusFileParser::getNext(char *buffer, int *length) {
    if (*length > decode_context_.bytes) {
        memcpy(buffer, decode_context_.packet, decode_context_.bytes);
        *length = decode_context_.bytes;

        if (decode_context_.e_o_s) {
            eof = true;
        }
    }
}

int OggOpusFileParser::getCodecType() {
    return 1;
}

int OggOpusFileParser::getSampleRateHz() {
    // return sampleRateHz_; // All Opus audio is coded at 48 kHz, and should also be decoded at 48
    // kHz
    return 48000;
}

int OggOpusFileParser::getNumberOfChannels() {
    return numberOfChannels_;
}

// HelperOpusFileParser 实现
HelperOpusFileParser::HelperOpusFileParser(const char *filepath) : file_path(filepath) {
}

HelperOpusFileParser::~HelperOpusFileParser() = default;

bool HelperOpusFileParser::initialize() {
    file_parser_.reset(new OggOpusFileParser(file_path.c_str()));

    if (!file_parser_ || !file_parser_->open()) {
        printf("Open opus file %s failed\n", file_path.c_str());
        return false;
    }
    printf("Open opus file %s successfully\n", file_path.c_str());
    return true;
}
void HelperOpusFileParser::setFileParseRestart() {
    file_parser_->reset();
}

std::unique_ptr<HelperAudioFrame> HelperOpusFileParser::getAudioFrame(int frameSizeDuration) {
    std::unique_ptr<HelperAudioFrame> audioFrame = nullptr;
    static uint8_t databuf[8192] = {0};
    static int length = 8192;
    static int bytesnum = 0;
    int numberOfChannels = file_parser_->getNumberOfChannels();
    int sampleRateHz = file_parser_->getSampleRateHz();
    int codec = file_parser_->getCodecType();
    // calculate Opus frame size
    int samplesPerChannel = file_parser_->getSampleRateHz() * frameSizeDuration / 1000;

    if (!file_parser_->hasNext()) {
        return nullptr;
    }
    length = 8192;
    file_parser_->getNext(reinterpret_cast<char *>(databuf), &length);
    if (length > 0) {
        unsigned char *buffer2 = new unsigned char[length];
        memcpy(buffer2, databuf, length);
        // ownsBuffer=true (default) because buffer2 was allocated with new[]
        audioFrame.reset(new HelperAudioFrame(numberOfChannels, sampleRateHz, codec,
                                              samplesPerChannel, buffer2, length, true));

        bytesnum += length;
    }
    return audioFrame;
}

std::vector<uint8_t> HelperOpusFileParser::getOggSHeader() const {
    return file_parser_ ? file_parser_->getOggSHeader() : std::vector<uint8_t>();
}

std::vector<uint8_t> HelperOpusFileParser::getOpusHeader() const {
    return file_parser_ ? file_parser_->getOpusHeader() : std::vector<uint8_t>();
}

std::vector<uint8_t> HelperOpusFileParser::getOggOpusTagsHeader() const {
    return file_parser_ ? file_parser_->getOggOpusTagsHeader() : std::vector<uint8_t>();
}

std::vector<uint8_t> HelperOpusFileParser::getOpusComments() const {
    return file_parser_ ? file_parser_->getOpusComments() : std::vector<uint8_t>();
}

std::vector<uint8_t> HelperOpusFileParser::getOggAudioHeader() const {
    return file_parser_ ? file_parser_->getOggAudioHeader() : std::vector<uint8_t>();
}
