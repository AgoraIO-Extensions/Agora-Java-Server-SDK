#!/bin/bash

if mvn install:install-file -Dfile=libs/agora-sdk.jar -DgroupId=io.agora.rtc -DartifactId=linux-java-sdk -Dversion=4.4.32.100 -Dpackaging=jar -Djavadoc=libs/agora-sdk-javadoc.jar; then
    echo "Install local jar to maven repository successfully."
else
    echo "Error: Install local jar to maven repository failed." >&2
    cd "$ORIGINAL_PWD"
    exit 1
fi
