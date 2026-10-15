#!/usr/bin/env sh

set -eu

XTREAM_PROJECT_ROOT_DIR=$(cd "$(dirname "$0")/../.." && pwd)
SUBPROJECT_ROOT_DIR="${XTREAM_PROJECT_ROOT_DIR}/xtream-codec-studio-app"
STUDIO_IMAGE_NAME="registry.cn-hangzhou.aliyuncs.com/xtream-codec/xtream-codec-studio-app"
XTREAM_PROJECT_VERSION=$(awk -F= '$1 == "projectVersion" { gsub(/[[:space:]]/, "", $2); print $2; exit }' \
    "${XTREAM_PROJECT_ROOT_DIR}/gradle.properties")

if [ -z "${XTREAM_PROJECT_VERSION}" ]; then
    echo "无法从 gradle.properties 读取 projectVersion" >&2
    exit 1
fi

echo "XTREAM_PROJECT_ROOT_DIR  : ${XTREAM_PROJECT_ROOT_DIR}"
echo "SUBPROJECT_ROOT_DIR      : ${SUBPROJECT_ROOT_DIR}"
echo "XTREAM_PROJECT_VERSION   : ${XTREAM_PROJECT_VERSION}"

cd "${XTREAM_PROJECT_ROOT_DIR}"

# 仅在手动构建镜像时生成包含 UI 的 fat JAR，日常构建保持按需打包。
./gradlew \
    -Pxtream.frontend.build.xtream-codec-studio-ui.enabled=true \
    -Pxtream.backend.build.checkstyle.enabled=true \
    :xtream-codec-studio-app:clean \
    :xtream-codec-studio-app:check \
    :xtream-codec-studio-app:shadowJar

export BUILDX_NO_DEFAULT_ATTESTATIONS=1
docker buildx build --platform linux/amd64,linux/arm64 \
    -t "${STUDIO_IMAGE_NAME}:latest" \
    -t "${STUDIO_IMAGE_NAME}:${XTREAM_PROJECT_VERSION}" \
    --build-arg "JAR_FILE=build/libs/xtream-codec-studio-app-${XTREAM_PROJECT_VERSION}-all.jar" \
    -f "${SUBPROJECT_ROOT_DIR}/docker/Dockerfile" \
    --push \
    "${SUBPROJECT_ROOT_DIR}"
