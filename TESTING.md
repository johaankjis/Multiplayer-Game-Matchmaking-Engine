# Testing Guide

## Test Structure

The project includes comprehensive testing at multiple levels:

1. **Unit Tests** - Test individual components in isolation
2. **Integration Tests** - Test components working together with Redis
3. **Load Tests** - Test performance under high concurrency
4. **API Tests** - Test REST endpoints end-to-end

## Running Tests

### All Tests

\`\`\`bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
\`\`\`

### Specific Test Classes

\`\`\`bash
# Run specific test class
mvn test -Dtest=MatchmakingAlgorithmTest

# Run specific test method
mvn test -Dtest=MatchmakingAlgorithmTest#testSkillBasedMatching
\`\`\`

### Integration Tests

\`\`\`bash
# Run integration tests (requires Docker for Testcontainers)
mvn verify
\`\`\`

### Load Tests

\`\`\`bash
# Run load tests
mvn test -Dtest=LoadTest
\`\`\`

## Test Coverage

### Generate Coverage Report

\`\`\`bash
mvn jacoco:report
\`\`\`

View the report at `target/site/jacoco/index.html`

### Coverage Goals

- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Method Coverage**: > 85%

## Performance Benchmarks

### Expected Results

Based on the MVP requirements:

1. **Match Accuracy**: ≥ 95%
   - Skill gap within configured threshold
   - Latency within acceptable range

2. **Throughput**: 50% improvement
   - Handle 1000+ requests/second
   - Process matchmaking in < 100ms

3. **Concurrency**: 10K+ concurrent users
   - No race conditions
   - Consistent queue ordering

### Running Benchmarks

\`\`\`bash
# Run load test
mvn test -Dtest=LoadTest

# Check results
cat target/surefire-reports/LoadTest.txt
\`\`\`

## Test Data

### Sample Players

\`\`\`json
{
  "playerId": "player123",
  "username": "TestPlayer",
  "skillRating": 1500,
  "latency": 50,
  "region": "us-east"
}
\`\`\`

### Test Scenarios

1. **Normal Matching**
   - 2 players, similar skill, low latency, same region
   - Expected: Match created successfully

2. **Skill Gap**
   - 2 players, skill difference > 200
   - Expected: No match created

3. **High Latency**
   - 2 players, latency > 100ms
   - Expected: No match created

4. **Different Regions**
   - 2 players, different regions
   - Expected: No match created

## Continuous Integration

### GitHub Actions

\`\`\`yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn verify
\`\`\`

## Troubleshooting

### Redis Connection Issues

If integration tests fail with Redis connection errors:

\`\`\`bash
# Ensure Docker is running
docker ps

# Check Testcontainers logs
docker logs $(docker ps -q --filter ancestor=redis:7-alpine)
\`\`\`

### Slow Tests

If tests are running slowly:

\`\`\`bash
# Run tests in parallel
mvn test -T 4

# Skip slow tests
mvn test -Dtest=!LoadTest
\`\`\`

## Best Practices

1. **Isolation**: Each test should be independent
2. **Cleanup**: Always clean up test data in @AfterEach
3. **Assertions**: Use descriptive assertion messages
4. **Naming**: Use clear, descriptive test names
5. **Coverage**: Aim for high coverage but focus on critical paths
