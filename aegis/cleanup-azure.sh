#!/bin/bash

# Azure cleanup script for AEGIS Security Backend

set -e

RESOURCE_GROUP="aegis-security-rg"

echo "🗑️ Cleaning up AEGIS Security Backend Azure resources..."

# Check if logged in to Azure
echo "📋 Checking Azure login status..."
az account show > /dev/null 2>&1 || {
    echo "❌ Not logged in to Azure. Please run 'az login' first."
    exit 1
}

# Check if resource group exists
if az group show --name $RESOURCE_GROUP &> /dev/null; then
    echo "⚠️ This will delete ALL resources in the resource group: $RESOURCE_GROUP"
    echo "This includes:"
    echo "  - Container Registry (aegissecurityacr)"
    echo "  - Container Instance (aegis-backend)"
    echo "  - Redis Cache (aegis-redis)"
    echo "  - All associated networking and storage resources"
    echo ""
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️ Deleting resource group and all resources..."
        az group delete --name $RESOURCE_GROUP --yes --no-wait
        echo "✅ Cleanup initiated. Resources will be deleted in the background."
        echo "📊 You can monitor the progress in the Azure portal."
    else
        echo "❌ Cleanup cancelled."
    fi
else
    echo "ℹ️ Resource group $RESOURCE_GROUP does not exist. Nothing to clean up."
fi
