# Easy Service Deployment

## One-Time Setup
Edit `config.sh` with your service details:
```bash
export SERVICE_NAME="your-service"
export DOCKER_TAG="v1.0.0"
export ENVIRONMENT="dev"
```

## Usage

### Quick Deploy (Build + Deploy):
```bash
./quick-deploy.sh
```

### Switch Service:
```bash
./switch-service.sh api-service prod v2.0.0
./quick-deploy.sh
```

### Individual Commands:
```bash
./auto-build.sh    # Build only
./auto-deploy.sh   # Deploy only
```

## Change Service
Just edit `config.sh` or use `switch-service.sh` - everything else updates automatically!