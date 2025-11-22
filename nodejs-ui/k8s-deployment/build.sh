#!/bin/bash

# Build and push Docker image
# Usage: ./build.sh <service-name> <tag> [dockerfile-path]

set -e

SERVICE_NAME=${1}
TAG=${2:-latest}
DOCKERFILE_PATH=${3:-.}
DOCKER_USERNAME="shahidkhan48"

if [ -z "$SERVICE_NAME" ]; then
    echo "Usage: ./build.sh <service-name> <tag> [dockerfile-path]"
    echo "Example: ./build.sh nodejs-ui v1.0.0"
    exit 1
fi

IMAGE_NAME="${DOCKER_USERNAME}/${SERVICE_NAME}:${TAG}"

echo "Building Docker image: ${IMAGE_NAME}"
docker build -t ${IMAGE_NAME} ${DOCKERFILE_PATH}

echo "Pushing to Docker registry..."
docker push ${IMAGE_NAME}

echo "Build and push completed!"
echo "Image: ${IMAGE_NAME}"