#!/bin/sh

java -jar -Dspring.profiles.active=$RUN_ENV -Dspring.application.name=$APP_NAME -Duser.timezone=Asia/Seoul $RUN_JVM_PARAM /usr/local/bin/cloud-application.jar
