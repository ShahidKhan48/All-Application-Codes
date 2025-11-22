#!/bin/bash

# Switch to different service configuration
# Usage: ./switch-service.sh <service-name> <environment> [docker-tag]

SERVICE_NAME=${1}
ENVIRONMENT=${2:-dev}
DOCKER_TAG=${3:-latest}

if [ -z "$SERVICE_NAME" ]; then
    echo "Usage: ./switch-service.sh <service-name> <environment> [docker-tag]"
    echo "Example: ./switch-service.sh api-service prod v2.0.0"
    exit 1
fi

# Update config.sh with new values
sed -i.bak "s/export SERVICE_NAME=.*/export SERVICE_NAME=\"${SERVICE_NAME}\"/" config.sh
sed -i.bak "s/export ENVIRONMENT=.*/export ENVIRONMENT=\"${ENVIRONMENT}\"/" config.sh
sed -i.bak "s/export DOCKER_TAG=.*/export DOCKER_TAG=\"${DOCKER_TAG}\"/" config.sh

echo "Configuration updated:"
echo "Service: ${SERVICE_NAME}"
echo "Environment: ${ENVIRONMENT}"
echo "Docker Tag: ${DOCKER_TAG}"
echo ""
echo "Now run: ./quick-deploy.sh"