#!/bin/bash

mvn install
[ $? -eq 0 ] && docker build --tag deckerego/docidx:0.1.0 --tag deckerego/docidx:latest .
