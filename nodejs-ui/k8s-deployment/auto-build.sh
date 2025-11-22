#!/bin/bash

# Auto build script using config.sh values
source ./config.sh

echo "Building ${SERVICE_NAME}:${DOCKER_TAG}..."

docker build -t ${DOCKER_USERNAME}/${SERVICE_NAME}:${DOCKER_TAG} .
docker push ${DOCKER_USERNAME}/${SERVICE_NAME}:${DOCKER_TAG}

echo "Build completed: ${DOCKER_USERNAME}/${SERVICE_NAME}:${DOCKER_TAG}"