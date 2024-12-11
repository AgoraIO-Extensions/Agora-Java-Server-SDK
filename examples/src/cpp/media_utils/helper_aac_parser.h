#include <string>
#include <memory>
#include "helper_audio_frame.h"

class HelperAacFileParser {
 public:
  HelperAacFileParser(const char* filepath);
  ~HelperAacFileParser();
  bool initialize();
  std::unique_ptr<HelperAudioFrame> getAudioFrame(int frameSizeDuration);
  void setFileParseRestart();
 private:
  std::string file_path_;
  int data_offset_;
  int data_size_;
  uint8_t* data_buffer_;
};
