#!/usr/bin/env sh

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../.."; pwd)
DASHBOARD_808_PROJECT_DIR="${XTREAM_PROJECT_ROOT_DIR}/ext/jt/jt-808-server-dashboard-spring-boot-starter-reactive"
TEMP_DI_TO_DELETE="${DASHBOARD_808_PROJECT_DIR}/src/main/resources/static/dashboard/808/"
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"
echo "DASHBOARD_808_PROJECT_DIR: ${DASHBOARD_808_PROJECT_DIR}"
echo "TEMP_DIR_TO_DELETE       : ${TEMP_DI_TO_DELETE}"

rm -rf "${TEMP_DI_TO_DELETE}"

cd ${XTREAM_PROJECT_ROOT_DIR}
echo "Working-Directory        : ${XTREAM_PROJECT_ROOT_DIR}"

./gradlew clean build publishMavenPublicationToMavenLocal \
-P buildJt808DashboardUi=true \
-P buildJt808DebugUi=false \
-P xtream.skip.checkstyle=false \
-P xtream.skip.fatjar=true
