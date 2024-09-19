#!/bin/bash
rm -fr build
mkdir -p build
find src -name "*.java" > build/test_source.txt
javac -g -cp .:third_party/commons-cli-1.5.0.jar:third_party/junit-4.13.2.jar:libs/agora-sdk.jar -encoding utf-8 @build/test_source.txt -d build  -XDignore.symbol.file
echo "build success"

