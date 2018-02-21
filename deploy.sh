#!/bin/bash

OPENCV_NATIVE_LIB='/usr/local/share/OpenCV/java/'

mvn install -DargLine="-Djava.library.path=$OPENCV_NATIVE_LIB"
[ $? -eq 0 ] && docker build --force-rm --tag deckerego/docidx:0.3.2 --tag deckerego/docidx:snapshot .
