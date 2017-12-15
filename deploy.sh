#!/bin/bash

mvn install
[ $? -eq 0 ] && docker build --tag deckerego/docidx .
