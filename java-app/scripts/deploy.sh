#!/bin/bash
set +e
APP_NAME=$1
APP_NAMESPACE=$2
APP_PROFILE=$3

echo "============================ Azure login ==============================="
az login --service-principal --username $AZURE_BITBUCKET_SP_APP_ID --password $AZURE_BITBUCKET_SP_PASSWORD --tenant $AZURE_BITBUCKET_SP_TENANT_ID
az aks get-credentials  --resource-group tf-nuke-app-$ENVIRONMENT_NAME --name $ENVIRONMENT_NAME-default-cluster --overwrite-existing

echo "============= Download and install KubeLogin ============="
wget https://github.com/Azure/kubelogin/releases/download/v0.0.25/kubelogin-linux-amd64.zip
unzip kubelogin-linux-amd64.zip
mv bin/linux_amd64/kubelogin /usr/local/bin
kubelogin convert-kubeconfig -l azurecli

echo "============= Download and install Helm ============="
wget https://raw.githubusercontent.com/helm/helm/master/scripts/get
mv get get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh  --version v3.9.3

echo "======= Clone ninja deployment ================="
git clone git@bitbucket.org:63idealabs/ninja-deployment.git

mv ninja-deployment/${APP_NAME}/ helm_values/
echo "======= Helm upgrade ============="
helm upgrade ${APP_NAME} ./ninja-service -n ${APP_NAMESPACE} -f ./helm_values/common.yaml -f ./helm_values/${APP_PROFILE}${ENVIRONMENT_NAME}.yaml --set image.tag=$(echo ${BITBUCKET_COMMIT:0:7}) --set secrets.NC_VAULT_TOKEN=${NC_VAULT_TOKEN} --set podAnnotations."bitbucketBuild"=\"${BITBUCKET_BUILD_NUMBER}\" --wait --install

