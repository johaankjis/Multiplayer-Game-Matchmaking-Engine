#!/bin/bash

# Script to run all tests

set -e

echo "Running unit tests..."
mvn test

echo ""
echo "Running integration tests..."
mvn verify

echo ""
echo "Generating test coverage report..."
mvn jacoco:report

echo ""
echo "All tests completed successfully!"
echo "Coverage report available at: target/site/jacoco/index.html"
