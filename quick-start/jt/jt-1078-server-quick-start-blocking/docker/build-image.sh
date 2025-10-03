#!/usr/bin/env sh

set -e

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../../../.."; pwd)
SUBPROJECT_ROOT_DIR="${XTREAM_PROJECT_ROOT_DIR}/quick-start/jt/jt-1078-server-quick-start-blocking"
echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"
echo "SUBPROJECT_ROOT_DIR      : ${SUBPROJECT_ROOT_DIR}"

cd ${XTREAM_PROJECT_ROOT_DIR}

./gradlew -P xtream.frontend.build.jt1078-quickstart-ui.blocking.enabled=true \
-P xtream.skip.fatjar=false \
-P xtream.skip.checkstyle=false \
:quick-start:jt:jt-1078-server-quick-start-blocking:clean \
:quick-start:jt:jt-1078-server-quick-start-blocking:build \

cd ${SUBPROJECT_ROOT_DIR}

#docker build -t jt-1078-server-quick-start-blocking:latest \
#-f ${SUBPROJECT_ROOT_DIR}/docker/Dockerfile \
#${SUBPROJECT_ROOT_DIR}

# 读取 gradle.properties 属性
function read_gradle_property() {
    if [[ -z "$1" || -z "$2" ]]; then
        echo "Usage: read_gradle_property <properties_file> <property_name>"
        return 1
    fi

    PROPERTIES_FILE="$1"
    PROPERTY_NAME="$2"

    if [[ ! -f "$PROPERTIES_FILE" ]]; then
        echo "Error: File '$PROPERTIES_FILE' not found!"
        return 1
    fi

    PROPERTY_VALUE=$(grep -E "^[^#]*$PROPERTY_NAME=" "$PROPERTIES_FILE" | sed -e "s/^$PROPERTY_NAME=//")

    if [[ -z "$PROPERTY_VALUE" ]]; then
        echo "Error: Property '$PROPERTY_NAME' not found in '$PROPERTIES_FILE'!"
        return 1
    fi

    echo "$PROPERTY_VALUE"
}

XTREAM_PROJECT_VERSION=$(read_gradle_property "${XTREAM_PROJECT_ROOT_DIR}/gradle.properties" "projectVersion")

export BUILDX_NO_DEFAULT_ATTESTATIONS=1
docker buildx build --platform linux/amd64,linux/arm64 \
-t registry.cn-hangzhou.aliyuncs.com/xtream-codec/jt-1078-server-quick-start-blocking:latest \
-t registry.cn-hangzhou.aliyuncs.com/xtream-codec/jt-1078-server-quick-start-blocking:${XTREAM_PROJECT_VERSION} \
-f ${SUBPROJECT_ROOT_DIR}/docker/Dockerfile \
${SUBPROJECT_ROOT_DIR} \
--push

