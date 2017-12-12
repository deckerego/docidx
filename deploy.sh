#!/bin/bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-9.0.1.jdk/Contents/Home

mvn install
[ $? -eq 0 ] && docker build --tag deckerego/docidx .
