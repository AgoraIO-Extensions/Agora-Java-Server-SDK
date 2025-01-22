#include "decode_media.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

static MediaPacket *currentPacket = nullptr;

jobject createJavaMediaPacket(JNIEnv *env, const MediaPacket &packet) {
    jclass mediaPacketClass = env->FindClass("io/agora/rtc/example/ffmpegutils/MediaDecode$MediaPacket");
    jmethodID constructor = env->GetMethodID(mediaPacketClass, "<init>", "()V");
    jobject jPacket = env->NewObject(mediaPacketClass, constructor);

    jfieldID fidBuffer = env->GetFieldID(mediaPacketClass, "buffer", "[B");
    jfieldID fidMediaType = env->GetFieldID(mediaPacketClass, "mediaType", "I");
    jfieldID fidPts = env->GetFieldID(mediaPacketClass, "pts", "J");
    jfieldID fidFlags = env->GetFieldID(mediaPacketClass, "flags", "I");
    jfieldID fidWidth = env->GetFieldID(mediaPacketClass, "width", "I");
    jfieldID fidHeight = env->GetFieldID(mediaPacketClass, "height", "I");
    jfieldID fidFramerateNum = env->GetFieldID(mediaPacketClass, "framerateNum", "I");
    jfieldID fidFramerateDen = env->GetFieldID(mediaPacketClass, "framerateDen", "I");

    env->SetIntField(jPacket, fidMediaType, packet.media_type);
    env->SetLongField(jPacket, fidPts, packet.pts);
    env->SetIntField(jPacket, fidFlags, packet.pkt->flags);
    env->SetIntField(jPacket, fidWidth, packet.width);
    env->SetIntField(jPacket, fidHeight, packet.height);
    env->SetIntField(jPacket, fidFramerateNum, packet.framerate_num);
    env->SetIntField(jPacket, fidFramerateDen, packet.framerate_den);

    if (packet.pkt && packet.pkt->data) {
        jbyteArray jBuffer = env->NewByteArray(packet.pkt->size);
        env->SetByteArrayRegion(jBuffer, 0, packet.pkt->size,
                                reinterpret_cast<const jbyte *>(packet.pkt->data));
        env->SetObjectField(jPacket, fidBuffer, jBuffer);
    }

    return jPacket;
}

jobject createJavaMediaFrame(JNIEnv *env, const MediaFrame &frame) {
    jclass frameClass = env->FindClass("io/agora/rtc/example/ffmpegutils/MediaDecode$MediaFrame");
    if (!frameClass) {
        return NULL; // Exception already thrown
    }

    jobject frameObject = env->AllocObject(frameClass);
    if (!frameObject) {
        return NULL; // OutOfMemoryError already thrown
    }

    jfieldID streamIndexField = env->GetFieldID(frameClass, "streamIndex", "I");
    jfieldID frameTypeField = env->GetFieldID(frameClass, "frameType", "I");
    jfieldID ptsField = env->GetFieldID(frameClass, "pts", "J");
    jfieldID bufferField = env->GetFieldID(frameClass, "buffer", "[B");
    jfieldID bufferSizeField = env->GetFieldID(frameClass, "bufferSize", "I");
    jfieldID formatField = env->GetFieldID(frameClass, "format", "I");
    jfieldID widthField = env->GetFieldID(frameClass, "width", "I");
    jfieldID heightField = env->GetFieldID(frameClass, "height", "I");
    jfieldID strideField = env->GetFieldID(frameClass, "stride", "I");
    jfieldID fpsField = env->GetFieldID(frameClass, "fps", "I");
    jfieldID samplesField = env->GetFieldID(frameClass, "samples", "I");
    jfieldID channelsField = env->GetFieldID(frameClass, "channels", "I");
    jfieldID sampleRateField = env->GetFieldID(frameClass, "sampleRate", "I");
    jfieldID bytesPerSampleField = env->GetFieldID(frameClass, "bytesPerSample", "I");

    if (!streamIndexField || !frameTypeField || !ptsField || !bufferField || !bufferSizeField ||
        !formatField || !widthField || !heightField || !strideField || !fpsField || !samplesField ||
        !channelsField || !sampleRateField || !bytesPerSampleField) {
        env->DeleteLocalRef(frameObject);
        env->ThrowNew(env->FindClass("java/lang/NoSuchFieldError"), "Field not found");
        return NULL;
    }

    // 设置字段值
    env->SetIntField(frameObject, streamIndexField, frame.stream_index);
    env->SetIntField(frameObject, frameTypeField, frame.frame_type);
    env->SetLongField(frameObject, ptsField, frame.pts);
    env->SetIntField(frameObject, bufferSizeField, frame.buffer_size);
    env->SetIntField(frameObject, formatField, frame.format);
    env->SetIntField(frameObject, widthField, frame.width);
    env->SetIntField(frameObject, heightField, frame.height);
    env->SetIntField(frameObject, strideField, frame.stride);
    env->SetIntField(frameObject, fpsField, frame.fps);
    env->SetIntField(frameObject, samplesField, frame.samples);
    env->SetIntField(frameObject, channelsField, frame.channels);
    env->SetIntField(frameObject, sampleRateField, frame.sample_rate);
    env->SetIntField(frameObject, bytesPerSampleField, frame.bytes_per_sample);

    jbyteArray byteArray = env->NewByteArray(frame.buffer_size);
    if (!byteArray) {
        env->DeleteLocalRef(frameObject);
        return NULL; // OutOfMemoryError already thrown
    }

    env->SetByteArrayRegion(byteArray, 0, frame.buffer_size, (const jbyte *)frame.buffer);
    env->SetObjectField(frameObject, bufferField, byteArray);

    env->DeleteLocalRef(byteArray);

    return frameObject;
}

MediaPacket convertToNativeMediaPacket(JNIEnv *env, jobject jPacket) {
    MediaPacket packet;
    memset(&packet, 0, sizeof(packet));

    jclass mediaPacketClass = env->GetObjectClass(jPacket);
    jfieldID fidPts = env->GetFieldID(mediaPacketClass, "pts", "J");

    packet.pts = env->GetLongField(jPacket, fidPts);

    return packet;
}

extern "C" {
JNIEXPORT jlong JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_openMediaFile(JNIEnv *env,
                                                                                jobject obj,
                                                                                jstring fileName) {
    const char *nativeFileName = NULL;
    void *decoder = NULL;

    if (!fileName) {
        return 0;
    }

    nativeFileName = env->GetStringUTFChars(fileName, NULL);
    if (!nativeFileName) {
        return 0;
    }

    decoder = open_media_file(nativeFileName);

    env->ReleaseStringUTFChars(fileName, nativeFileName);

    if (!decoder) {
        return 0;
    }

    return (jlong)decoder;
}

JNIEXPORT jlong JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_getMediaDuration(JNIEnv *env,
                                                                                   jobject obj,
                                                                                   jlong decoder) {
    if (!decoder) {
        return 0;
    }

    return get_media_duration((void *)decoder);
}

JNIEXPORT jobject JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_getFrame(JNIEnv *env,
                                                                             jobject obj,
                                                                             jlong decoder) {
    if (!decoder) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "Decoder is null");
        return NULL;
    }

    MediaFrame mediaFrame;

    int result = get_frame((void *)decoder, &mediaFrame);
    if (result != 0) {
        jclass mediaDecodeClass = env->GetObjectClass(obj);
        jfieldID isEndOfFileReachedFieldID =
            env->GetFieldID(mediaDecodeClass, "isEndOfFileReached", "Z");
        env->SetBooleanField(obj, isEndOfFileReachedFieldID, true);
        return NULL;
    }

    return createJavaMediaFrame(env, mediaFrame);
}

JNIEXPORT void JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_closeMediaFile(JNIEnv *env,
                                                                                jobject obj,
                                                                                jlong decoder) {
    if (decoder) {
        close_media_file((void *)decoder);
    }
    currentPacket = nullptr;
}

JNIEXPORT jobject JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_getPacket(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong decoder) {
    if (decoder) {
        int result = get_packet(reinterpret_cast<void *>(decoder), &currentPacket);
        if (result != 0) {
            jclass mediaDecodeClass = env->GetObjectClass(thiz);
            jfieldID isEndOfFileReachedFieldID =
                env->GetFieldID(mediaDecodeClass, "isEndOfFileReached", "Z");
            env->SetBooleanField(thiz, isEndOfFileReachedFieldID, true);
            return nullptr;
        }

        if (currentPacket == nullptr) {
            return nullptr;
        }
        if (currentPacket->pts < 0) {
            currentPacket->pts = 1;
        }
        jobject jPacket = createJavaMediaPacket(env, *currentPacket);
        return jPacket;
    }
    return nullptr;
}

JNIEXPORT jint JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_freePacket(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong decoder,
                                                                            jobject jPacket) {
    int result = free_packet(&currentPacket);
    currentPacket = nullptr;
    return result;
}

JNIEXPORT jobject JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_convertH264ToAnnexB(
    JNIEnv *env, jobject thiz, jlong decoder, jobject jPacket) {
    if (decoder) {
        int result = h264_to_annexb(reinterpret_cast<void *>(decoder), &currentPacket);
        if (result != 0 || currentPacket == nullptr) {
            return nullptr;
        }
        jobject jConvertedPacket = createJavaMediaPacket(env, *currentPacket);
        return jConvertedPacket;
    } else {
        return nullptr;
    }
}

JNIEXPORT jobject JNICALL Java_io_agora_rtc_example_ffmpegutils_MediaDecode_decodePacket(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jlong decoder,
                                                                                 jobject jPacket) {
    if (decoder) {
        MediaFrame frame;
        memset(&frame, 0, sizeof(frame));
        int result = decode_packet(reinterpret_cast<void *>(decoder), currentPacket, &frame);
        if (result != 0) {
            return nullptr;
        }
        return createJavaMediaFrame(env, frame);
    } else {
        return nullptr;
    }
}
}