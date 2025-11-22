#!/bin/bash

# Quick deploy - build and deploy in one command
source ./config.sh

echo "=== Quick Deploy: ${SERVICE_NAME} ==="
echo "Environment: ${ENVIRONMENT}"
echo "Docker Tag: ${DOCKER_TAG}"
echo ""

echo "Step 1: Building Docker image..."
./auto-build.sh

echo ""
echo "Step 2: Deploying to Kubernetes..."
./auto-deploy.sh

echo ""
echo "=== Deployment Complete ==="
echo "Access your service at: http://${INGRESS_HOST_DEV}"