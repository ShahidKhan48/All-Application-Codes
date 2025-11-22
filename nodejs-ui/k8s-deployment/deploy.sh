#!/bin/bash

# Deploy to Kubernetes using Helm charts
# Usage: ./deploy.sh <service-name> <environment> [docker-tag]

set -e

SERVICE_NAME=${1}
ENVIRONMENT=${2:-dev}
DOCKER_TAG=${3:-latest}

if [ -z "$SERVICE_NAME" ]; then
    echo "Usage: ./deploy.sh <service-name> <environment> [docker-tag]"
    echo "Example: ./deploy.sh nodejs-ui dev v1.0.0"
    exit 1
fi

CHART_PATH="./helm-charts"
NAMESPACE=${SERVICE_NAME}
DOCKER_IMAGE="shahidkhan48/${SERVICE_NAME}:${DOCKER_TAG}"

echo "Deploying ${SERVICE_NAME} to ${ENVIRONMENT} environment..."
echo "Image: ${DOCKER_IMAGE}"

# Create namespace
kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

# Deploy with Helm (includes secret, configmap, deployment, service)
helm upgrade --install ${SERVICE_NAME} ${CHART_PATH} \
  --namespace ${NAMESPACE} \
  --values ${CHART_PATH}/common.yml \
  --values ${CHART_PATH}/${ENVIRONMENT}.yml \
  --set image.repository=shahidkhan48/${SERVICE_NAME} \
  --set image.tag=${DOCKER_TAG} \
  --set fullnameOverride=${SERVICE_NAME} \
  --wait

echo "Deployment completed!"
echo "Service: ${SERVICE_NAME}"
echo "Environment: ${ENVIRONMENT}"
echo "Namespace: ${NAMESPACE}"