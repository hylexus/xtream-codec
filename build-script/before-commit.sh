#!/bin/bash

set -e

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/.."; pwd)
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}

./gradlew clean build \
-P xtream.backend.build.checkstyle.enabled=true \
-P xtream.backend.build.errorprone.enabled=true \
-P xtream.backend.build.license-checker.enabled=true \
-P xtream.maven.repo.central-portal.enabled=false \
-P xtream.maven.repo.private.enabled=false \
-P xtream.maven.repo.github.enabled=false \
-P xtream.maven.publications.signing=off
