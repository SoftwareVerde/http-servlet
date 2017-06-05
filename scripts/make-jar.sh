#!/bin/bash

rm -rf out
mkdir out
cp -R example-data/* out/.

./gradlew makeJar

cp $(ls -tr build/libs/*.jar | tail -1) out/server.jar

