# Deployment Guide

## Quick Start with Docker Compose

### 1. Basic Deployment

\`\`\`bash
# Build and start services
docker-compose up --build -d

# Check logs
docker-compose logs -f

# Stop services
docker-compose down
\`\`\`

### 2. Scaled Deployment (Multiple Instances)

\`\`\`bash
# Scale to 3 instances with load balancer
docker-compose -f docker-compose.yml -f docker-compose.scale.yml up -d --scale matchmaking-engine=3

# Access via load balancer
curl http://localhost:80/actuator/health
\`\`\`

### 3. Development Mode

\`\`\`bash
# Use development Dockerfile with hot reload
docker-compose -f docker-compose.dev.yml up
\`\`\`

## Kubernetes Deployment

### 1. Create Secrets

\`\`\`bash
kubectl create secret generic matchmaking-secrets \
  --from-literal=jwt-secret=your-secret-key
\`\`\`

### 2. Deploy Application

\`\`\`bash
# Apply deployments
kubectl apply -f kubernetes/deployment.yaml

# Apply horizontal pod autoscaler
kubectl apply -f kubernetes/hpa.yaml

# Check status
kubectl get pods
kubectl get svc
\`\`\`

### 3. Access Application

\`\`\`bash
# Get external IP
kubectl get svc matchmaking-service

# Port forward for local testing
kubectl port-forward svc/matchmaking-service 8080:80
\`\`\`

## Performance Tuning

### JVM Options

For production, adjust JVM settings in docker-compose.yml:

\`\`\`yaml
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m -XX:+UseG1GC
\`\`\`

### Redis Configuration

For high throughput, adjust Redis settings:

\`\`\`bash
redis-server --maxmemory 1gb --maxmemory-policy allkeys-lru
\`\`\`

## Monitoring

### Health Checks

\`\`\`bash
# Application health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics
\`\`\`

### Redis Monitoring

\`\`\`bash
# Connect to Redis CLI
docker exec -it matchmaking-redis redis-cli

# Check queue size
ZCARD matchmaking:queue

# Monitor commands
MONITOR
\`\`\`

## Load Testing

\`\`\`bash
# Run load test script
./scripts/load-test.sh http://localhost:8080 100 10000

# Or use Apache Bench directly
ab -n 10000 -c 100 http://localhost:8080/api/matchmaking/queueStatus
\`\`\`

## Troubleshooting

### Container Logs

\`\`\`bash
# View application logs
docker logs matchmaking-engine

# View Redis logs
docker logs matchmaking-redis
\`\`\`

### Common Issues

1. **Connection Refused**: Check if Redis is healthy
   \`\`\`bash
   docker-compose ps
   \`\`\`

2. **Out of Memory**: Increase JVM heap size in JAVA_OPTS

3. **Slow Performance**: Check Redis memory usage and adjust maxmemory

## Production Checklist

- [ ] Change JWT_SECRET to a strong random value
- [ ] Configure proper resource limits
- [ ] Set up monitoring and alerting
- [ ] Enable HTTPS/TLS
- [ ] Configure backup for Redis data
- [ ] Set up log aggregation
- [ ] Configure rate limiting appropriately
- [ ] Test disaster recovery procedures
