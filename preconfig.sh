#!/bin/sh
docker run --rm -u 1000:108 -v /var/log:/var/log -v "$PWD":/home/gradle/project -w /home/gradle/project \
  -e SPRING_PROFILES_ACTIVE='dev,disable-discovery' gradle:jdk11 /home/gradle/project/gradlew clean build --info
