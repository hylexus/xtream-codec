#!/usr/bin/env sh

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../.."; pwd)
DASHBOARD_808_PROJECT_DIR="${XTREAM_PROJECT_ROOT_DIR}/ext/jt/jt-808-server-dashboard-spring-boot-starter-reactive"
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"
echo "DASHBOARD_808_PROJECT_DIR: ${DASHBOARD_808_PROJECT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}
echo "Working-Directory        : ${XTREAM_PROJECT_ROOT_DIR}"

rm -rf /tmp/gradle/maven-tmp
rm -rf /tmp/gradle/maven-tmp1

# org.gradle.parallel=false @see https://stackoverflow.com/questions/72664149/gradle-maven-publish-sonatype-creates-multiple-repositories-that-cant-be-clos
# https://central.sonatype.org/publish/publish-portal-api/

./gradlew clean build publishToMavenCentralPortal \
-P xtream.maven.repo.central-portal.enabled=true \
-P xtream.maven.publications.signing=on \
-P xtream.frontend.build.jt808-dashboard-ui.enabled=true \
-P xtream.skip.checkstyle=false \
-P xtream.skip.fatjar=true
