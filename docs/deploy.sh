#!/bin/bash

set -e

XTREAM_DOCS_ROOT_DIR=$(cd "$(dirname "$0")"; pwd)
echo "XTREAM_DOCS_ROOT_DIR  : ${XTREAM_DOCS_ROOT_DIR}"

cd ${XTREAM_DOCS_ROOT_DIR}

echo "Now at: $(pwd)"

pnpm install
pnpm run docs:build

# 进入生成的文件夹
cd src/.vuepress/dist
#
git init
git add -A
git commit -m 'deploy docs'
#
git push -f git@github.com:hylexus/xtream-codec.git main:gh-pages
