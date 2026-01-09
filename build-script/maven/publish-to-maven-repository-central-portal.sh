#!/usr/bin/env sh

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../.."; pwd)
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}
echo "Working-Directory        : ${XTREAM_PROJECT_ROOT_DIR}"

# @see xtream.maven.repo.central-portal.artifacts.temp-dir
# @see xtream.maven.repo.central-portal.bom.temp-dir
rm -rf /tmp/xtream-codec/temp-artifacts
rm -rf /tmp/xtream-codec/temp-bom

# org.gradle.parallel=false @see https://stackoverflow.com/questions/72664149/gradle-maven-publish-sonatype-creates-multiple-repositories-that-cant-be-clos
# https://central.sonatype.org/publish/publish-portal-api/

./gradlew clean build publishToMavenCentralPortal \
-P xtream.maven.repo.central-portal.enabled=true \
-P xtream.maven.publications.signing=on \
-P xtream.frontend.build.jt-808-server-dashboard-ui.enabled=true \
-P xtream.backend.build.checkstyle.enabled=true \
-P xtream.backend.build.errorprone.enabled=true \
-P xtream.backend.build.license-checker.enabled=true \
-P xtream.backend.build.debug-module-fatjar.enabled=false
