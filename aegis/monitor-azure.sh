#!/bin/bash

# Azure monitoring script for AEGIS Security Backend

RESOURCE_GROUP="aegis-security-rg"
CONTAINER_NAME="aegis-backend"
REDIS_NAME="aegis-redis"

echo "📊 AEGIS Security Backend - Azure Monitoring Dashboard"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if logged in to Azure
az account show > /dev/null 2>&1 || {
    echo "❌ Not logged in to Azure. Please run 'az login' first."
    exit 1
}

# Check if resource group exists
if ! az group show --name $RESOURCE_GROUP &> /dev/null; then
    echo "❌ Resource group $RESOURCE_GROUP does not exist."
    echo "Run ./deploy-azure.sh to deploy the application first."
    exit 1
fi

echo "✅ Resource Group: $RESOURCE_GROUP"

# Container Instance Status
echo ""
echo "🐳 Container Instance Status:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME &> /dev/null; then
    CONTAINER_STATE=$(az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --query instanceView.state --output tsv)
    CONTAINER_FQDN=$(az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --query ipAddress.fqdn --output tsv)
    CONTAINER_IP=$(az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --query ipAddress.ip --output tsv)
    
    echo "📍 Name: $CONTAINER_NAME"
    echo "🔄 State: $CONTAINER_STATE"
    echo "🌐 FQDN: $CONTAINER_FQDN"
    echo "📍 Public IP: $CONTAINER_IP"
    echo "🔗 Application URL: http://$CONTAINER_FQDN:8080/api"
    echo "🔗 Health Check: http://$CONTAINER_FQDN:8080/api/actuator/health"
else
    echo "❌ Container instance not found"
fi

# Redis Cache Status
echo ""
echo "🔴 Redis Cache Status:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if az redis show --name $REDIS_NAME --resource-group $RESOURCE_GROUP &> /dev/null; then
    REDIS_STATUS=$(az redis show --name $REDIS_NAME --resource-group $RESOURCE_GROUP --query provisioningState --output tsv)
    REDIS_HOST=$(az redis show --name $REDIS_NAME --resource-group $RESOURCE_GROUP --query hostName --output tsv)
    REDIS_PORT=$(az redis show --name $REDIS_NAME --resource-group $RESOURCE_GROUP --query sslPort --output tsv)
    
    echo "📍 Name: $REDIS_NAME"
    echo "🔄 Status: $REDIS_STATUS"
    echo "🌐 Host: $REDIS_HOST"
    echo "🔌 SSL Port: $REDIS_PORT"
else
    echo "❌ Redis cache not found"
fi

# Recent Container Logs
echo ""
echo "📝 Recent Container Logs:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME &> /dev/null; then
    az container logs --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --tail 20
else
    echo "❌ Container not found - cannot retrieve logs"
fi

echo ""
echo "🔧 Useful Commands:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 View live logs: az container logs --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --follow"
echo "🔄 Restart container: az container restart --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME"
echo "🔍 Container details: az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME"
echo "🗑️ Cleanup resources: ./cleanup-azure.sh"
