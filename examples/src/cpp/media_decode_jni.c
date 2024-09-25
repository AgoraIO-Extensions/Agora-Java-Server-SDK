#include "decode_media.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

// JNI 方法实现
JNIEXPORT jlong JNICALL Java_io_agora_rtc_mediautils_MediaDecode_openMediaFile(JNIEnv *env,
                                                                               jobject obj,
                                                                               jstring fileName) {
    const char *nativeFileName = NULL;
    void *decoder = NULL;

    if (!fileName) {
        return 0;
    }

    nativeFileName = (*env)->GetStringUTFChars(env, fileName, NULL);
    if (!nativeFileName) {
        return 0;
    }

    decoder = open_media_file(nativeFileName);

    (*env)->ReleaseStringUTFChars(env, fileName, nativeFileName);

    if (!decoder) {
        return 0;
    }

    return (jlong)decoder;
}

JNIEXPORT jlong JNICALL Java_io_agora_rtc_mediautils_MediaDecode_getMediaDuration(JNIEnv *env,
                                                                                  jobject obj,
                                                                                  jlong decoder) {
    if (!decoder) {
        return 0;
    }

    return get_media_duration((void *)decoder);
}

JNIEXPORT jobject JNICALL Java_io_agora_rtc_mediautils_MediaDecode_getFrame(JNIEnv *env,
                                                                            jobject obj,
                                                                            jlong decoder) {
    MediaFrame mediaFrame;
    jobject frameObject = NULL;
    jclass frameClass = NULL;
    jbyteArray byteArray = NULL;

    if (!decoder) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/IllegalArgumentException"),
                         "Decoder is null");
        return NULL;
    }

    // 调用获取帧的函数
    int result = get_frame((void *)decoder, &mediaFrame);
    if (result != 0) {
        return NULL;
    }

    // 创建 MediaFrame 对象
    frameClass = (*env)->FindClass(env, "io/agora/rtc/mediautils/MediaDecode$MediaFrame");
    if (!frameClass) {
        return NULL; // Exception already thrown
    }

    frameObject = (*env)->AllocObject(env, frameClass);
    if (!frameObject) {
        return NULL; // OutOfMemoryError already thrown
    }

    // 设置字段值
    jfieldID streamIndexField = (*env)->GetFieldID(env, frameClass, "streamIndex", "I");
    jfieldID frameTypeField = (*env)->GetFieldID(env, frameClass, "frameType", "I");
    jfieldID ptsField = (*env)->GetFieldID(env, frameClass, "pts", "J");
    jfieldID bufferField = (*env)->GetFieldID(env, frameClass, "buffer", "[B");
    jfieldID bufferSizeField = (*env)->GetFieldID(env, frameClass, "bufferSize", "I");
    jfieldID formatField = (*env)->GetFieldID(env, frameClass, "format", "I");

    jfieldID widthField = (*env)->GetFieldID(env, frameClass, "width", "I");
    jfieldID heightField = (*env)->GetFieldID(env, frameClass, "height", "I");
    jfieldID strideField = (*env)->GetFieldID(env, frameClass, "stride", "I");
    jfieldID fpsField = (*env)->GetFieldID(env, frameClass, "fps", "I");

    jfieldID samplesField = (*env)->GetFieldID(env, frameClass, "samples", "I");
    jfieldID channelsField = (*env)->GetFieldID(env, frameClass, "channels", "I");
    jfieldID sampleRateField = (*env)->GetFieldID(env, frameClass, "sampleRate", "I");
    jfieldID bytesPerSampleField = (*env)->GetFieldID(env, frameClass, "bytesPerSample", "I");

    if (!streamIndexField || !frameTypeField || !ptsField || !bufferField || !bufferSizeField ||
        !formatField || !widthField || !heightField || !strideField || !fpsField || !samplesField ||
        !channelsField || !sampleRateField || !bytesPerSampleField) {
        (*env)->DeleteLocalRef(env, frameObject);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/NoSuchFieldError"),
                         "Field not found");
        return NULL;
    }

    // 设置字段值
    (*env)->SetIntField(env, frameObject, streamIndexField, mediaFrame.stream_index);
    (*env)->SetIntField(env, frameObject, frameTypeField, mediaFrame.frame_type);
    (*env)->SetLongField(env, frameObject, ptsField, mediaFrame.pts);
    (*env)->SetIntField(env, frameObject, bufferSizeField, mediaFrame.buffer_size);
    (*env)->SetIntField(env, frameObject, formatField, mediaFrame.format);

    (*env)->SetIntField(env, frameObject, widthField, mediaFrame.width);
    (*env)->SetIntField(env, frameObject, heightField, mediaFrame.height);
    (*env)->SetIntField(env, frameObject, strideField, mediaFrame.stride);
    (*env)->SetIntField(env, frameObject, fpsField, mediaFrame.fps);

    (*env)->SetIntField(env, frameObject, samplesField, mediaFrame.samples);
    (*env)->SetIntField(env, frameObject, channelsField, mediaFrame.channels);
    (*env)->SetIntField(env, frameObject, sampleRateField, mediaFrame.sample_rate);
    (*env)->SetIntField(env, frameObject, bytesPerSampleField, mediaFrame.bytes_per_sample);

    // 处理 buffer
    byteArray = (*env)->NewByteArray(env, mediaFrame.buffer_size);
    if (!byteArray) {
        (*env)->DeleteLocalRef(env, frameObject);
        return NULL; // OutOfMemoryError already thrown
    }

    (*env)->SetByteArrayRegion(env, byteArray, 0, mediaFrame.buffer_size,
                               (const jbyte *)mediaFrame.buffer);
    (*env)->SetObjectField(env, frameObject, bufferField, byteArray);

    (*env)->DeleteLocalRef(env, byteArray);

    return frameObject; // 返回 MediaFrame 对象
}

JNIEXPORT void JNICALL Java_io_agora_rtc_mediautils_MediaDecode_closeMediaFile(JNIEnv *env,
                                                                               jobject obj,
                                                                               jlong decoder) {
    if (decoder) {
        close_media_file((void *)decoder);
    }
}
