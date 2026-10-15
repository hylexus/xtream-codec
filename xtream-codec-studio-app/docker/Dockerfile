# Copyright 2024-present the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM m.daocloud.io/docker.io/library/eclipse-temurin:21-jre
LABEL authors="hylexus"

RUN mkdir -p /app /data

WORKDIR /app
ARG JAR_FILE=build/libs/*-all.jar
COPY ${JAR_FILE} /app/app.jar

ENV XTREAM_STUDIO_HOST=0.0.0.0 \
    XTREAM_STUDIO_PORT=8765 \
    XTREAM_STUDIO_DATA_DIR=/data

EXPOSE 8765
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
