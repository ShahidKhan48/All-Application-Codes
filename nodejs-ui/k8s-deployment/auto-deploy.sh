#!/bin/bash

# Auto deploy script using config.sh values
source ./config.sh

CHART_PATH="./helm-charts"
NAMESPACE=${SERVICE_NAME}

echo "Deploying ${SERVICE_NAME} to ${ENVIRONMENT}..."

# Create namespace
kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

# Set environment-specific values
if [ "$ENVIRONMENT" = "prod" ]; then
    REPLICAS=$REPLICAS_PROD
    NODE_ENV=$NODE_ENV_PROD
    API_URL=$API_URL_PROD
    INGRESS_HOST=$INGRESS_HOST_PROD
else
    REPLICAS=$REPLICAS_DEV
    NODE_ENV=$NODE_ENV_DEV
    API_URL=$API_URL_DEV
    INGRESS_HOST=$INGRESS_HOST_DEV
fi

# Deploy with Helm
helm upgrade --install ${SERVICE_NAME} ${CHART_PATH} \
  --namespace ${NAMESPACE} \
  --values ${CHART_PATH}/common.yml \
  --values ${CHART_PATH}/${ENVIRONMENT}.yml \
  --set image.repository=${DOCKER_USERNAME}/${SERVICE_NAME} \
  --set image.tag=${DOCKER_TAG} \
  --set fullnameOverride=${SERVICE_NAME} \
  --set replicaCount=${REPLICAS} \
  --set service.targetPort=${SERVICE_PORT} \
  --set imageCredentials.username=${DOCKER_USERNAME} \
  --set imageCredentials.password=${DOCKER_PASSWORD} \
  --set config.NODE_ENV=${NODE_ENV} \
  --set config.PORT=${SERVICE_PORT} \
  --set config.API_URL=${API_URL} \
  --set ingress.hosts[0].host=${INGRESS_HOST} \
  --set resources.requests.cpu=${CPU_REQUEST} \
  --set resources.requests.memory=${MEMORY_REQUEST} \
  --set resources.limits.cpu=${CPU_LIMIT} \
  --set resources.limits.memory=${MEMORY_LIMIT} \
  --wait

echo "Deployment completed!"
echo "Service: ${SERVICE_NAME}"
echo "Environment: ${ENVIRONMENT}"
echo "Image: ${DOCKER_USERNAME}/${SERVICE_NAME}:${DOCKER_TAG}"