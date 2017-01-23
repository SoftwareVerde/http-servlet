#!/bin/bash

./gradlew makeJar

echo -e "\nJar Location: "
ls -tr build/libs/*.jar | tail -1
