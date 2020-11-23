#!/bin/sh

export SPRING_PROFILES_ACTIVE='dev,disable-discovery'
./gradlew clean build --info
