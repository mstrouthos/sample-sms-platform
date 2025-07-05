#!/bin/bash

# Build script for all Quarkus services
echo "Building all Quarkus services..."

# Build SMS Service
echo "Building SMS Service..."
cd sms-service
mvn clean package -DskipTests
cd ..

# Build second service (uncomment and modify as needed)
# echo "Building Notification Service..."
# cd notification-service
# mvn clean package -DskipTests
# cd ..

echo "All services built successfully!"
echo "Run 'docker-compose up --build' to start all services"