#!/usr/bin/env sh

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../../../.."; pwd)
SUBPROJECT_ROOT_DIR="${XTREAM_PROJECT_ROOT_DIR}/quick-start/jt/jt-808-server-quick-start-with-storage"
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"
echo "SUBPROJECT_ROOT_DIR      : ${SUBPROJECT_ROOT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}

./gradlew -P buildJt808DashboardUi=true \
-P buildQuickstartUi=true \
:quick-start:jt:jt-808-server-quick-start-with-storage:clean \
:quick-start:jt:jt-808-server-quick-start-with-storage:build \

cd ${SUBPROJECT_ROOT_DIR}

#docker build -t jt-808-server-quick-start-with-storage:latest \
#-f ${SUBPROJECT_ROOT_DIR}/docker/Dockerfile \
#${SUBPROJECT_ROOT_DIR}

export BUILDX_NO_DEFAULT_ATTESTATIONS=1
docker buildx build --platform linux/amd64,linux/arm64 \
-t registry.cn-hangzhou.aliyuncs.com/xtream-codec/jt-808-server-quick-start-with-storage:latest \
-t registry.cn-hangzhou.aliyuncs.com/xtream-codec/jt-808-server-quick-start-with-storage:0.0.1-beta.10 \
-f ${SUBPROJECT_ROOT_DIR}/docker/Dockerfile \
${SUBPROJECT_ROOT_DIR} \
--push

