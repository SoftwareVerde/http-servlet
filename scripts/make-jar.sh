#!/bin/bash

rm bin/*.jar
./gradlew makeJar && cp build/libs/http-servlet-0.0.1.jar bin/main.jar && chmod 770 bin/main.jar

echo "Done."
echo 

