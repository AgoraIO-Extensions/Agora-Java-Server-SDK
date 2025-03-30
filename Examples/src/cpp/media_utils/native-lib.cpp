#include "helper_aac_parser.h"
#include "helper_h264_parser.h"
#include "helper_opus_parser.h"
#include "helper_vp8_parser.h"
#include <jni.h>
#include <mutex>
#include <string>

static jclass clsH264Frame;
static jmethodID constructorH264Frame;

static jclass clsAacFrame;
static jmethodID constructorAacFrame;

static jclass clsAudioFrame;
static jmethodID constructorAudioFrame;

static jclass clsVideoFrame;
static jmethodID constructorVideoFrame;

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }

    if (clsH264Frame != nullptr) {
        env->DeleteGlobalRef(clsH264Frame);
        clsH264Frame = nullptr;
        constructorH264Frame = nullptr;
    }

    if (clsAacFrame) {
        env->DeleteGlobalRef(clsAacFrame);
        clsAacFrame = nullptr;
        constructorAacFrame = nullptr;
    }

    if (clsAudioFrame) {
        env->DeleteGlobalRef(clsAudioFrame);
        clsAudioFrame = nullptr;
        constructorAudioFrame = nullptr;
    }

    if (clsVideoFrame != nullptr) {
        env->DeleteGlobalRef(clsVideoFrame);
        clsVideoFrame = nullptr;
        constructorVideoFrame = nullptr;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_kcg_ctemplate_MainActivity_stringFromJNI(JNIEnv *env, jobject obj /* this */) {
    std::string hello = "Hello from C++";
    jclass objCls = env->FindClass("java/lang/Object");
    jclass currentCls = env->GetObjectClass(obj);
    jmethodID getNameMid = env->GetMethodID(objCls, "toString", "()Ljava/lang/String;");
    jobject name = env->CallObjectMethod(obj, getNameMid);
    return env->NewStringUTF(hello.c_str());
}
static std::mutex init_mutex;

extern "C" JNIEXPORT jlong JNICALL
Java_io_agora_rtc_example_mediautils_H264Reader_init(JNIEnv *env, jobject thiz, jstring path) {
    std::lock_guard<std::mutex> lock(init_mutex);
    const char *filePath = env->GetStringUTFChars(path, NULL);
    HelperH264FileParser *helperH264FileParser = new HelperH264FileParser(filePath);
    helperH264FileParser->initialize();
    if (clsH264Frame == nullptr) {
        jclass cls = env->FindClass("io/agora/rtc/example/mediautils/H264Reader$H264Frame");
        if (cls != nullptr) {
            clsH264Frame = (jclass)env->NewGlobalRef(cls);
            constructorH264Frame = env->GetMethodID(cls, "<init>", "([BI)V");
        } else {
            abort();
        }
        env->DeleteLocalRef(cls);
    }
    if (constructorH264Frame == nullptr) {
        abort();
    }

    env->ReleaseStringUTFChars(path, filePath);
    return reinterpret_cast<long>(helperH264FileParser);
}

extern "C" JNIEXPORT jobject JNICALL Java_io_agora_rtc_example_mediautils_H264Reader_getNextFrame(
    JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr == 0L) {
        return nullptr;
    }

    try {
        HelperH264FileParser *helperH264FileParser = reinterpret_cast<HelperH264FileParser *>(cptr);
        if (helperH264FileParser == nullptr) {
            return nullptr;
        }

        std::unique_ptr<HelperH264Frame> frame = helperH264FileParser->getH264Frame();
        if (frame != nullptr && frame->buffer.get() != nullptr && frame->bufferLen > 0 &&
            nullptr != clsH264Frame) {
            jbyteArray arrys = env->NewByteArray(frame->bufferLen);
            if (arrys == nullptr) {
                return nullptr;
            }

            env->SetByteArrayRegion(arrys, 0, frame->bufferLen, (const jbyte *)frame->buffer.get());

            jobject data = env->NewObject(clsH264Frame, constructorH264Frame, arrys,
                                          frame->isKeyFrame ? 3 : 4);
            return data;
        }
    } catch (const std::exception &e) {
        const char *msg = e.what();
        if (msg != nullptr) {
        }
    } catch (...) {
    }

    return nullptr;
}

extern "C" JNIEXPORT void JNICALL Java_io_agora_rtc_example_mediautils_H264Reader_nativeRelease(
    JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr == 0L)
        return;
    try {
        HelperH264FileParser *helperH264FileParser = reinterpret_cast<HelperH264FileParser *>(cptr);
        if (helperH264FileParser != nullptr) {
            delete helperH264FileParser;
        }
    } catch (...) {
        // Capture all exceptions to prevent JVM crashes
    }
    return;
}

extern "C" JNIEXPORT void JNICALL
Java_io_agora_rtc_example_mediautils_H264Reader_nativeReset(JNIEnv *env, jobject thiz, jlong cptr) {
    HelperH264FileParser *helperH264FileParser = reinterpret_cast<HelperH264FileParser *>(cptr);
    if (helperH264FileParser != nullptr) {
        helperH264FileParser->setFileParseRestart();
    }
    return;
}

extern "C" JNIEXPORT jlong JNICALL
Java_io_agora_rtc_example_mediautils_AacReader_init(JNIEnv *env, jobject thiz, jstring path) {
    if (path == nullptr)
        return 0L;
    const char *cpath = env->GetStringUTFChars(path, NULL);
    HelperAacFileParser *helperAacFileParser = new HelperAacFileParser(cpath);
    helperAacFileParser->initialize();
    env->ReleaseStringUTFChars(path, cpath);
    jclass cls = env->FindClass("io/agora/rtc/example/mediautils/AacReader$AacFrame");
    if (cls != nullptr) {
        clsAacFrame = (jclass)env->NewGlobalRef(cls);
        constructorAacFrame = env->GetMethodID(cls, "<init>", "(IIIII[B)V");
        if (constructorAacFrame == nullptr) {
            abort();
        }
        env->DeleteLocalRef(cls);
    } else {
        abort();
    }

    return reinterpret_cast<long>(helperAacFileParser);
}

extern "C" JNIEXPORT jobject JNICALL Java_io_agora_rtc_example_mediautils_AacReader_getAacFrame(
    JNIEnv *env, jobject thiz, jlong cptr, jint frame_size_duration) {
    if (cptr == 0L)
        return nullptr;
    HelperAacFileParser *helperAacFileParser = reinterpret_cast<HelperAacFileParser *>(cptr);
    std::unique_ptr<HelperAudioFrame> frame =
        helperAacFileParser->getAudioFrame(frame_size_duration);
    if (frame != nullptr) {
        jbyteArray arrys = env->NewByteArray(frame.get()->bufferLen);
        env->SetByteArrayRegion(arrys, 0, frame.get()->bufferLen,
                                (const jbyte *)frame.get()->buffer);
        jobject data =
            env->NewObject(clsAacFrame, constructorAacFrame, frame.get()->numberOfChannels,
                           frame.get()->sampleRate, frame.get()->codec,
                           frame.get()->samplesPerChannel, frame.get()->bufferLen, arrys);
        return data;
    }
    return nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_io_agora_rtc_example_mediautils_AacReader_release(JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr != 0L) {
        HelperAacFileParser *helperAacFileParser = reinterpret_cast<HelperAacFileParser *>(cptr);
        if (helperAacFileParser != nullptr) {
            delete helperAacFileParser;
        }
    }

    return;
}

extern "C" JNIEXPORT void JNICALL
Java_io_agora_rtc_example_mediautils_AacReader_nativeReset(JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr == 0L)
        return;
    HelperAacFileParser *helperAacFileParser = reinterpret_cast<HelperAacFileParser *>(cptr);
    if (helperAacFileParser != nullptr) {
        helperAacFileParser->setFileParseRestart();
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_init(JNIEnv *env, jobject thiz, jstring path) {
    if (path == nullptr)
        return 0L;
    const char *cpath = env->GetStringUTFChars(path, NULL);
    HelperOpusFileParser *helperOpusFileParser = new HelperOpusFileParser(cpath);
    helperOpusFileParser->initialize();
    env->ReleaseStringUTFChars(path, cpath);
    jclass cls = env->FindClass("io/agora/rtc/example/mediautils/AudioFrame");
    if (cls != nullptr) {
        clsAudioFrame = (jclass)env->NewGlobalRef(cls);
        constructorAudioFrame = env->GetMethodID(cls, "<init>", "(IIIII[B)V");
        if (constructorAudioFrame == nullptr) {
            abort();
        }
        env->DeleteLocalRef(cls);
    } else {
        abort();
    }

    return reinterpret_cast<long>(helperOpusFileParser);
}

extern "C" JNIEXPORT jobject JNICALL Java_io_agora_rtc_example_mediautils_OpusReader_getOpusFrame(
    JNIEnv *env, jobject thiz, jlong cptr, jint frame_size_duration) {
    if (cptr == 0L)
        return nullptr;
    HelperOpusFileParser *helperOpusFileParser = reinterpret_cast<HelperOpusFileParser *>(cptr);
    std::unique_ptr<HelperAudioFrame> frame =
        helperOpusFileParser->getAudioFrame(frame_size_duration);
    if (frame != nullptr) {
        jbyteArray arrys = env->NewByteArray(frame.get()->bufferLen);
        env->SetByteArrayRegion(arrys, 0, frame.get()->bufferLen,
                                (const jbyte *)frame.get()->buffer);
        delete frame.get()->buffer;
        jobject data =
            env->NewObject(clsAudioFrame, constructorAudioFrame, frame.get()->numberOfChannels,
                           frame.get()->sampleRate, frame.get()->codec,
                           frame.get()->samplesPerChannel, frame.get()->bufferLen, arrys);
        return data;
    }
    return nullptr;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_nativeGetOggSHeader(JNIEnv *env, jobject thiz,
                                                                    jlong cptr) {
    if (cptr == 0L)
        return nullptr;
    HelperOpusFileParser *parser = reinterpret_cast<HelperOpusFileParser *>(cptr);
    if (!parser) {
        return nullptr;
    }

    std::vector<uint8_t> header = parser->getOggSHeader();
    jbyteArray result = env->NewByteArray(header.size());
    if (result) {
        env->SetByteArrayRegion(result, 0, header.size(),
                                reinterpret_cast<const jbyte *>(header.data()));
    }
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_nativeGetOpusHeader(JNIEnv *env, jobject thiz,
                                                                    jlong cptr) {
    if (cptr == 0L)
        return nullptr;
    HelperOpusFileParser *parser = reinterpret_cast<HelperOpusFileParser *>(cptr);
    if (!parser) {
        return nullptr;
    }

    std::vector<uint8_t> header = parser->getOpusHeader();
    jbyteArray result = env->NewByteArray(header.size());
    if (result) {
        env->SetByteArrayRegion(result, 0, header.size(),
                                reinterpret_cast<const jbyte *>(header.data()));
    }
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_nativeOggGetOpusTagsHeader(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong cptr) {
    if (cptr == 0L)
        return nullptr;
    HelperOpusFileParser *parser = reinterpret_cast<HelperOpusFileParser *>(cptr);
    if (!parser) {
        return nullptr;
    }

    std::vector<uint8_t> header = parser->getOggOpusTagsHeader();
    jbyteArray result = env->NewByteArray(header.size());
    if (result) {
        env->SetByteArrayRegion(result, 0, header.size(),
                                reinterpret_cast<const jbyte *>(header.data()));
    }

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_nativeGetOpusComments(JNIEnv *env, jobject thiz,
                                                                      jlong cptr) {
    if (cptr == 0L)
        return nullptr;
    HelperOpusFileParser *parser = reinterpret_cast<HelperOpusFileParser *>(cptr);
    if (!parser) {
        return nullptr;
    }

    std::vector<uint8_t> comments = parser->getOpusComments();
    jbyteArray result = env->NewByteArray(comments.size());
    if (result) {
        env->SetByteArrayRegion(result, 0, comments.size(),
                                reinterpret_cast<const jbyte *>(comments.data()));
    }
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_nativeGetOggAudioHeader(JNIEnv *env, jobject thiz,
                                                                        jlong cptr) {
    if (cptr == 0L)
        return nullptr;
    HelperOpusFileParser *parser = reinterpret_cast<HelperOpusFileParser *>(cptr);
    if (!parser) {
        return nullptr;
    }

    std::vector<uint8_t> header = parser->getOggAudioHeader();
    jbyteArray result = env->NewByteArray(header.size());
    if (result) {
        env->SetByteArrayRegion(result, 0, header.size(),
                                reinterpret_cast<const jbyte *>(header.data()));
    }

    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_nativeReset(JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr == 0L)
        return;
    HelperOpusFileParser *helperOpusFileParser = reinterpret_cast<HelperOpusFileParser *>(cptr);
    if (helperOpusFileParser != nullptr) {
        helperOpusFileParser->setFileParseRestart();
    }
    return;
}

extern "C" JNIEXPORT void JNICALL
Java_io_agora_rtc_example_mediautils_OpusReader_release(JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr == 0L)
        return;
    HelperOpusFileParser *helperOpusFileParser = reinterpret_cast<HelperOpusFileParser *>(cptr);
}

extern "C" JNIEXPORT jlong JNICALL
Java_io_agora_rtc_example_mediautils_Vp8Reader_init(JNIEnv *env, jobject thiz, jstring path) {
    const char *filePath = env->GetStringUTFChars(path, NULL);
    HelperVp8FileParser *helperVp8FileParser = new HelperVp8FileParser(filePath);
    helperVp8FileParser->initialize();
    jclass cls = env->FindClass("io/agora/rtc/example/mediautils/VideoFrame");
    if (cls != nullptr) {
        clsVideoFrame = (jclass)env->NewGlobalRef(cls);
        constructorVideoFrame = env->GetMethodID(cls, "<init>", "(IIIIII[B)V");
        if (constructorVideoFrame == nullptr) {
            abort();
        }
        env->DeleteLocalRef(cls);
    } else {
        abort();
    }
    env->ReleaseStringUTFChars(path, filePath);
    return reinterpret_cast<long>(helperVp8FileParser);
}

extern "C" JNIEXPORT jobject JNICALL
Java_io_agora_rtc_example_mediautils_Vp8Reader_getNextFrame(JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr == 0L)
        return nullptr;
    HelperVp8FileParser *helperVp8FileParser = reinterpret_cast<HelperVp8FileParser *>(cptr);
    std::unique_ptr<HelperVideoFrame> frame = helperVp8FileParser->getVp8Frame();
    if (frame != nullptr) {
        jbyteArray arrys = env->NewByteArray(frame.get()->bufferLen);
        env->SetByteArrayRegion(arrys, 0, frame.get()->bufferLen,
                                (const jbyte *)frame.get()->buffer.get());
        jobject data =
            env->NewObject(clsVideoFrame, constructorVideoFrame, frame.get()->width,
                           frame.get()->height, frame.get()->codec, frame.get()->rotation,
                           frame.get()->frametype, frame.get()->bufferLen, arrys);
        return data;
    }
    return nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_io_agora_rtc_example_mediautils_Vp8Reader_release(JNIEnv *env, jobject thiz, jlong cptr) {
    if (cptr == 0L)
        return;
    HelperVp8FileParser *helperVp8FileParser = reinterpret_cast<HelperVp8FileParser *>(cptr);
    if (helperVp8FileParser != nullptr) {
        delete helperVp8FileParser;
    }
    return;
}

extern "C" JNIEXPORT void JNICALL
Java_io_agora_rtc_example_mediautils_Vp8Reader_nativeReset(JNIEnv *env, jobject thiz, jlong cptr) {
    HelperVp8FileParser *helperVp8FileParser = reinterpret_cast<HelperVp8FileParser *>(cptr);
    if (helperVp8FileParser != nullptr) {
        helperVp8FileParser->setFileParseRestart();
    }
    return;
}
