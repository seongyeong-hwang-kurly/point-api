FROM registry.kurlycorp.kr/baseimages/openjdk11:latest

COPY build/libs/point-api.jar /usr/local/bin/cloud-application.jar
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]