#!/bin/bash

# Script to scale matchmaking engine horizontally

set -e

REPLICAS=${1:-3}

echo "Scaling matchmaking engine to $REPLICAS replicas..."

docker-compose -f docker-compose.yml -f docker-compose.scale.yml up -d --scale matchmaking-engine=$REPLICAS

echo "Scaled to $REPLICAS replicas successfully!"
echo "Load balancer available at: http://localhost:80"
