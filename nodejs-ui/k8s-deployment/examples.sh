#!/bin/bash

# Example usage commands

echo "Example commands:"
echo ""

echo "1. Build and push Docker image:"
echo "./build.sh nodejs-ui v1.0.0"
echo ""

echo "2. Deploy to dev environment:"
echo "./deploy.sh nodejs-ui dev v1.0.0"
echo ""

echo "3. Deploy to production:"
echo "./deploy.sh api-service prod latest"
echo ""

echo "4. Complete workflow:"
echo "./build.sh my-app v2.0.0 && ./deploy.sh my-app dev v2.0.0"
echo ""

echo "5. Undeploy a service:"
echo "./undeploy.sh nodejs-ui"