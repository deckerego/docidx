FROM ubuntu:latest

MAINTAINER john@deckerego.net

RUN apt-get --assume-yes update
RUN apt-get --assume-yes install default-jre

ARG DOCIDX_VERSION=0.0.1-SNAPSHOT
ADD target/docmag-${DOCIDX_VERSION}.jar /opt/docidx/docidx.jar

WORKDIR /opt/docidx

ENTRYPOINT [ "java", "-jar", "docidx.jar"]