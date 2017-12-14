FROM ubuntu:latest

MAINTAINER john@deckerego.net

RUN apt-get --assume-yes update
RUN apt-get --assume-yes install openjdk-8-jre tesseract-ocr

ARG DOCIDX_VERSION=0.0.1-SNAPSHOT
ADD target/docidx-${DOCIDX_VERSION}.jar /opt/docidx/docidx.jar

VOLUME /mnt/docs

WORKDIR /opt/docidx

ENTRYPOINT [ "java", "-jar", "docidx.jar"]
