#include <memory>
#include <string>

struct HelperH265Frame {
    bool isKeyFrame;
    std::unique_ptr<uint8_t[]> buffer;
    int bufferLen;
};

class HelperH265FileParser {
   public:
    HelperH265FileParser(const char* filepath);
    ~HelperH265FileParser();

    std::unique_ptr<HelperH265Frame> getH265Frame();
    bool initialize();
    void setFileParseRestart();

   private:
    void _getH265Frame(std::unique_ptr<HelperH265Frame>& h265Frame, bool is_key_frame,
                       int frame_start, int frame_end);

    std::string file_path_;
    int data_offset_;
    int data_size_;
    uint8_t* data_buffer_;
};
