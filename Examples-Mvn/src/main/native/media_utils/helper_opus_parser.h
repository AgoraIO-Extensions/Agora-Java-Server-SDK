#include "helper_audio_frame.h"
#include <memory>
#include <string>
#include <vector>
// #include "AgoraBase.h"

class OggOpusFileParser;

class HelperOpusFileParser {
  public:
    HelperOpusFileParser(const char *filepath);
    ~HelperOpusFileParser();
    bool initialize();
    std::unique_ptr<HelperAudioFrame> getAudioFrame(int frameSizeDuration);
    void setFileParseRestart();
    std::vector<uint8_t> getOggSHeader() const;
    std::vector<uint8_t> getOpusHeader() const;
    std::vector<uint8_t> getOggOpusTagsHeader() const;
    std::vector<uint8_t> getOpusComments() const;
    std::vector<uint8_t> getOggAudioHeader() const;

  private:
    std::string file_path;
    std::unique_ptr<OggOpusFileParser> file_parser_;
    int64_t sent_audio_frames_{0};
};
