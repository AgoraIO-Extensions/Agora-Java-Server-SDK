#!/usr/bin/env zsh
#sh run.zsh io.agora.rtc.test.H264PcmTest -channelId  aga -token 123 -sampleRate 23900

export DYLD_LIBRARY_PATH=`pwd`/libs/
export LD_LIBRARY_PATH=`pwd`/libs/
echo "$LD_LIBRARY_PATH"
CLASS=$1
shift 1
java -cp .:third_party/commons-cli-1.5.0.jar:third_party/junit-4.13.2.jar:./libs/agora-sdk.jar:./build -Xcheck:jni -XX:+HeapDumpOnOutOfMemoryError  -Djava.library.path=libs $CLASS $* | grep -v "WARNING in native method: JNI call made without checking exceptions"