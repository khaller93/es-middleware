#!/bin/bash
set -e

java -Djava.security.egd=file:/dev/./urandom -jar /app.jar "$@"
