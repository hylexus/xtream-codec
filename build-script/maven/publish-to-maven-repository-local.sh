#!/usr/bin/env sh

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../.."; pwd)
DASHBOARD_808_PROJECT_DIR="${XTREAM_PROJECT_ROOT_DIR}/ext/jt/jt-808-server-dashboard-spring-boot-starter-reactive"
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"
echo "DASHBOARD_808_PROJECT_DIR: ${DASHBOARD_808_PROJECT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}
echo "Working-Directory        : ${XTREAM_PROJECT_ROOT_DIR}"

./gradlew clean build publishMavenPublicationToMavenLocal publishMavenBomPublicationToMavenLocal \
-P xtream.frontend.build.jt-808-server-dashboard-ui.enabled=true \
-P xtream.backend.build.checkstyle.enabled=true \
-P xtream.backend.build.debug-module-fatjar.enabled=false
