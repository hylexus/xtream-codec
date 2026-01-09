#!/usr/bin/env sh

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../.."; pwd)
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}
echo "Working-Directory        : ${XTREAM_PROJECT_ROOT_DIR}"

./gradlew clean build publishMavenPublicationToMavenLocal publishMavenBomPublicationToMavenLocal \
-P xtream.frontend.build.jt-808-server-dashboard-ui.enabled=true \
-P xtream.backend.build.checkstyle.enabled=true \
-P xtream.backend.build.errorprone.enabled=true \
-P xtream.backend.build.license-checker.enabled=true \
-P xtream.backend.build.debug-module-fatjar.enabled=false
