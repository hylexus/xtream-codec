#!/bin/bash

set -e

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../.."; pwd)
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}

${XTREAM_PROJECT_ROOT_DIR}/gradlew clean build -P xtream.skip.checkstyle=false
