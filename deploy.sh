#!/bin/bash

mvn install
docker build --tag deckerego/docidx .
[ $? -eq 0 ] && docker push deckerego/docidx
