# Kubernetes Deployment with Helm

This directory contains Helm charts and deployment scripts for easy Kubernetes deployment of any service.

## Structure
```
k8s-deployment/
├── helm-charts/           # Helm chart files
│   ├── templates/         # Kubernetes manifest templates
│   ├── Chart.yaml        # Chart metadata
│   ├── values.yaml       # Default values
│   ├── common.yml        # Common configuration
│   ├── dev.yml          # Development environment
│   └── prod.yml         # Production environment
├── manifest/             # Static manifests (for reference)
├── deploy.sh            # Main deployment script
├── undeploy.sh          # Undeployment script
└── examples.sh          # Usage examples

```

## Quick Start

### Deploy a service:
```bash
./deploy.sh <service-name> <docker-image> <environment>
```

### Examples:
```bash
# Deploy nodejs-ui service
./deploy.sh nodejs-ui myregistry/nodejs-ui:v1.0.0 dev

# Deploy API service  
./deploy.sh api-service myregistry/api:latest prod

# Deploy with default nginx image
./deploy.sh my-app nginx:latest dev
```

### Undeploy:
```bash
./undeploy.sh <service-name>
```

## Configuration

- **common.yml**: Shared configuration across all environments
- **dev.yml**: Development-specific settings
- **prod.yml**: Production-specific settings

## Adding New Environments

Create a new `.yml` file in `helm-charts/` directory with environment-specific values.

## Customization

Modify the values in environment files to customize:
- Docker image repository and tags
- Resource limits and requests
- Ingress hosts and TLS settings
- Environment variables
- Replica counts