FROM ubuntu:bionic

MAINTAINER john@deckerego.net

# Install Java and OpenCV
RUN apt-get --assume-yes update
RUN apt-get --assume-yes install openjdk-8-jre libopencv3.2-jni

# Build chain for Tesseract 4
RUN apt-get --assume-yes install build-essential git autoconf autoconf-archive wget cmake zlib1g-dev libcairo2-dev libicu-dev libjpeg8-dev libpango1.0-dev libtiff5-dev

# Build Leptonica
RUN git clone https://github.com/DanBloomberg/leptonica.git
WORKDIR leptonica
RUN autoreconf -vi
RUN ./autobuild
RUN ./configure
RUN make
RUN make install
WORKDIR ..
RUN rm -r -f leptonica

# Get Tesseract 4 trained data files
RUN mkdir /usr/local/share/tessdata
RUN wget -O /usr/local/share/tessdata/osd.traineddata https://github.com/tesseract-ocr/tessdata/raw/4.00/osd.traineddata
RUN wget -O /usr/local/share/tessdata/equ.traineddata https://github.com/tesseract-ocr/tessdata/raw/4.00/equ.traineddata
RUN wget -O /usr/local/share/tessdata/eng.traineddata https://github.com/tesseract-ocr/tessdata/raw/4.00/eng.traineddata

# Build Tesseract 4
RUN git clone https://github.com/tesseract-ocr/tesseract.git
WORKDIR tesseract
RUN ./autogen.sh
RUN ./configure
RUN make
RUN make install
RUN ldconfig
RUN make training
RUN make training-install
WORKDIR ..
RUN rm -r -f tesseract

# Clean up Tesseract 4 buildchain
RUN apt-get --assume-yes remove build-essential git autoconf autoconf-archive wget cmake zlib1g-dev libcairo2-dev libicu-dev libjpeg8-dev libpango1.0-dev libtiff5-dev
RUN apt-get --assume-yes autoremove

# Install DocIndex
ARG DOCIDX_VERSION=0.3.0
ADD target/docidx-${DOCIDX_VERSION}.jar /opt/docidx/docidx.jar

VOLUME /mnt/docs

WORKDIR /opt/docidx

ENTRYPOINT [ "java", "-Djava.library.path=/usr/lib/jni/", "-jar", "docidx.jar"]
