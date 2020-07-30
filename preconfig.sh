#!/bin/sh
docker run --rm -u 1000:108 -v /var/log:/var/log -v "$PWD":/home/gradle/project -w /home/gradle/project \
  -e SPRING_PROFILES_ACTIVE='dev,disable-discovery' -e TZ=Asia/Seoul -e JAVA_OPTS='-Xmx8g' gradle:jdk11 /home/gradle/project/gradlew clean build --info
