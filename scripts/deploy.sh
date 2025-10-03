#!/bin/bash

# Deployment script for matchmaking engine

set -e

echo "Building Docker image..."
docker build -t matchmaking-engine:latest .

echo "Starting services with Docker Compose..."
docker-compose up -d

echo "Waiting for services to be healthy..."
sleep 10

echo "Checking health status..."
curl -f http://localhost:8080/actuator/health || exit 1

echo "Deployment successful!"
echo "API available at: http://localhost:8080"
echo "Redis Commander available at: http://localhost:8081 (if debug profile enabled)"
