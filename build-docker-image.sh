#!/bin/sh
set -x

mvn clean package -DskipTests=true
cd appcore && mvn dockerfile:build
