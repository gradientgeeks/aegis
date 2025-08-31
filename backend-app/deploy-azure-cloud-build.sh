#!/bin/bash

# Azure deployment script for UCO Bank Demo Backend (Cloud Build)
# Southeast Asia deployment with ACR Build (no local Docker required)

set -e

# Configuration
RESOURCE_GROUP="demo-backend-rg"
LOCATION="southeastasia"
ACR_NAME="demobackendacr"
CONTAINER_NAME="demo-backend"
IMAGE_NAME="demo-backend"
TAG="latest"
REDIS_NAME="ucobank-redis-$(date +%s)"

echo "🏦 Starting UCO Bank Demo Backend deployment to Azure Southeast Asia (Cloud Build)..."

# Check if logged in to Azure
echo "📋 Checking Azure login status..."
az account show > /dev/null 2>&1 || {
    echo "❌ Not logged in to Azure. Please run 'az login' first."
    exit 1
}

# Check if JAR exists
if [ ! -f "build/libs/backend-app-0.0.1-SNAPSHOT.jar" ]; then
    echo "❌ JAR file not found. Please run './gradlew bootJar' first."
    exit 1
fi

echo "✅ Pre-built JAR found!"

# Create resource group (skip if exists)
echo "📦 Checking/Creating resource group..."
az group create \
    --name $RESOURCE_GROUP \
    --location $LOCATION \
    --output table || echo "Resource group already exists"

# Create Azure Container Registry (skip if exists)
echo "🏗️ Checking/Creating Azure Container Registry..."
az acr create \
    --resource-group $RESOURCE_GROUP \
    --name $ACR_NAME \
    --sku Basic \
    --location $LOCATION \
    --admin-enabled true \
    --output table || echo "Container registry already exists"

# Get ACR login server
ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --resource-group $RESOURCE_GROUP --query loginServer --output tsv)
echo "📍 ACR Login Server: $ACR_LOGIN_SERVER"

# Build image in Azure Container Registry (no local Docker needed)
echo "🔨 Building Docker image in Azure Container Registry..."
echo "📤 Uploading source code and building remotely..."

# Create a temporary directory for build context
BUILD_CONTEXT_DIR=$(mktemp -d)
echo "📁 Creating build context in: $BUILD_CONTEXT_DIR"

# Copy necessary files to build context
cp build/libs/backend-app-0.0.1-SNAPSHOT.jar "$BUILD_CONTEXT_DIR/"

# Create a custom Dockerfile for the build context
cat > "$BUILD_CONTEXT_DIR/Dockerfile" << 'EOF'
# Optimized Dockerfile using pre-built JAR
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health check
RUN apk add --no-cache curl

# Copy the pre-built jar (now in root of build context)
COPY backend-app-0.0.1-SNAPSHOT.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 appuser && adduser -D -u 1001 -G appuser appuser && chown appuser:appuser app.jar
USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
EOF

# Copy .dockerignore if it exists
if [ -f ".dockerignore" ]; then
    cp .dockerignore "$BUILD_CONTEXT_DIR/"
fi

# Build using ACR Build
az acr build \
    --registry $ACR_NAME \
    --image $IMAGE_NAME:$TAG \
    --file "$BUILD_CONTEXT_DIR/Dockerfile" \
    "$BUILD_CONTEXT_DIR"

# Clean up build context
rm -rf "$BUILD_CONTEXT_DIR"

echo "✅ Image built and pushed to ACR successfully!"

# Create Redis Cache (skip if exists)
echo "🔴 Checking for existing Redis Cache..."

# First, try to find any existing Redis instance in the resource group
EXISTING_REDIS=$(az redis list --resource-group $RESOURCE_GROUP --query "[?provisioningState=='Succeeded'].name | [0]" --output tsv)

if [ ! -z "$EXISTING_REDIS" ] && [ "$EXISTING_REDIS" != "null" ]; then
    echo "✅ Found existing Redis cache: $EXISTING_REDIS"
    REDIS_NAME=$EXISTING_REDIS
else
    echo "🔴 Using Redis name: $REDIS_NAME"
    
    # Check if our specific Redis already exists
    if az redis show --name $REDIS_NAME --resource-group $RESOURCE_GROUP > /dev/null 2>&1; then
        echo "✅ Redis cache $REDIS_NAME already exists"
    else
        echo "📦 Creating new Redis cache: $REDIS_NAME (this may take 15-20 minutes)..."
        echo "⏰ Consider using an existing Redis instance to save time"
        az redis create \
            --resource-group $RESOURCE_GROUP \
            --name $REDIS_NAME \
            --location $LOCATION \
            --sku Basic \
            --vm-size c0 \
            --output table
        
        if [ $? -ne 0 ]; then
            echo "❌ Failed to create Redis cache. Trying with a different name..."
            REDIS_NAME="ucobank-redis-backup-$(date +%s)"
            echo "🔄 Retrying with name: $REDIS_NAME"
            az redis create \
                --resource-group $RESOURCE_GROUP \
                --name $REDIS_NAME \
                --location $LOCATION \
                --sku Basic \
                --vm-size c0 \
                --output table
        fi
    fi
fi

# Get Redis connection details
REDIS_HOST=$(az redis show --name $REDIS_NAME --resource-group $RESOURCE_GROUP --query hostName --output tsv)
REDIS_KEY=$(az redis list-keys --name $REDIS_NAME --resource-group $RESOURCE_GROUP --query primaryKey --output tsv)

echo "🔴 Redis Host: $REDIS_HOST"

# Get Aegis API URL (assuming it's already deployed)
AEGIS_API_URL="http://aegis-backend-api.southeastasia.azurecontainer.io:8080/api"

# Create Container Instance (delete if exists first)
echo "🐳 Creating Azure Container Instance..."
az container delete --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --yes || echo "No existing container to delete"
az container create \
    --resource-group $RESOURCE_GROUP \
    --name $CONTAINER_NAME \
    --image $ACR_LOGIN_SERVER/$IMAGE_NAME:$TAG \
    --registry-login-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_NAME \
    --registry-password $(az acr credential show --name $ACR_NAME --resource-group $RESOURCE_GROUP --query passwords[0].value --output tsv) \
    --dns-name-label demo-backend-api \
    --ports 8081 \
    --cpu 2 \
    --memory 4 \
    --os-type Linux \
    --location $LOCATION \
    --environment-variables \
        SPRING_PROFILES_ACTIVE=prod \
        SPRING_DATA_REDIS_HOST=$REDIS_HOST \
        SPRING_DATA_REDIS_PORT=6380 \
        SPRING_DATA_REDIS_PASSWORD=$REDIS_KEY \
        SPRING_DATA_REDIS_SSL_ENABLED=true \
        AEGIS_API_BASE_URL=$AEGIS_API_URL \
        UCOBANK_SERVICE_DISCOVERY_AEGIS_API_URL=$AEGIS_API_URL \
        SERVER_PORT=8081 \
        CORS_ALLOWED_ORIGINS="*" \
        DATABASE_URL=jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/ucobank_db_v3 \
        DATABASE_USERNAME=4ASi2rWg4pDfPqV.root \
        DATABASE_PASSWORD=LrdiTvlCRA4VDQKH \
    --output table

# Get container instance details
CONTAINER_FQDN=$(az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --query ipAddress.fqdn --output tsv)
CONTAINER_IP=$(az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --query ipAddress.ip --output tsv)

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "📊 Deployment Summary:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🏦 Service: Demo Bank Demo Backend for Hackathon"
echo "🌍 Region: Southeast Asia ($LOCATION)"
echo "📦 Resource Group: $RESOURCE_GROUP"
echo "🏗️ Container Registry: $ACR_LOGIN_SERVER"
echo "🐳 Container Instance: $CONTAINER_NAME"
echo "🔴 Redis Cache: $REDIS_HOST"
echo "🌐 Application URL: http://$CONTAINER_FQDN:8081/api"
echo "📍 Public IP: $CONTAINER_IP"
echo "🔗 Aegis API: $AEGIS_API_URL"
echo "💾 Database: TiDB Cloud (ucobank_db_v3)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔍 To check application logs:"
echo "az container logs --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --follow"
echo ""
echo "🔄 To restart the container:"
echo "az container restart --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME"
echo ""
echo "🗑️ To clean up resources:"
echo "az group delete --name $RESOURCE_GROUP --yes --no-wait"
echo ""
echo "🎉 Demo Bank Demo Backend is now running in Azure Southeast Asia!"