#!/bin/bash

rm -rf out/bin 2>/dev/null
mkdir -p out/bin

./gradlew makeJar && cp build/libs/*.jar out/bin/. && chmod 770 out/bin/*.jar

