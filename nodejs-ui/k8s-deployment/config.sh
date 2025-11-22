#!/bin/bash

# Configuration file for all services
# Edit this file to change service settings

# Service Configuration
export SERVICE_NAME="nodejs-ui"
export SERVICE_PORT="3000"
export DOCKER_TAG="v1.0.0"
export ENVIRONMENT="dev"

# Docker Registry
export DOCKER_USERNAME="shahidkhan48"
export DOCKER_PASSWORD="your-password"

# Ingress Configuration
export INGRESS_HOST_DEV="dev.example.com"
export INGRESS_HOST_PROD="example.com"

# Resource Configuration
export CPU_REQUEST="250m"
export CPU_LIMIT="500m"
export MEMORY_REQUEST="256Mi"
export MEMORY_LIMIT="512Mi"

# Replica Configuration
export REPLICAS_DEV="1"
export REPLICAS_PROD="3"

# Application Configuration
export NODE_ENV_DEV="development"
export NODE_ENV_PROD="production"
export API_URL_DEV="https://dev-api.example.com"
export API_URL_PROD="https://api.example.com"