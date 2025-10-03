#!/bin/bash

# Load testing script using Apache Bench

set -e

API_URL=${1:-http://localhost:8080}
CONCURRENT=${2:-100}
REQUESTS=${3:-10000}

echo "Running load test..."
echo "API URL: $API_URL"
echo "Concurrent requests: $CONCURRENT"
echo "Total requests: $REQUESTS"

# Generate JWT token first
TOKEN=$(curl -s -X POST "$API_URL/api/auth/token" \
  -H "Content-Type: application/json" \
  -d '{"playerId":"test-player","password":"test"}' | jq -r '.data.token')

echo "Token obtained: ${TOKEN:0:20}..."

# Create test data file
cat > /tmp/matchmaking-test.json <<EOF
{
  "playerId": "player-\$RANDOM",
  "username": "TestPlayer",
  "skillRating": 1500,
  "latency": 50,
  "region": "us-east"
}
EOF

# Run load test
ab -n $REQUESTS -c $CONCURRENT \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -p /tmp/matchmaking-test.json \
  "$API_URL/api/matchmaking/joinQueue"

echo "Load test completed!"
