# Centralized Test Suite

This folder contains comprehensive unit tests for all microservices in the project.

## Structure

- `config/` - Test configuration files
- `inventory-service/` - Inventory service tests
- `transaction-service/` - Transaction service tests
- `personnel-service/` - Personnel service tests
- `store-service/` - Store service tests
- `api-gateway/` - API Gateway tests
- `frontend-service/` - Frontend service tests
- `integration/` - Integration tests
- `utils/` - Test utilities and helpers

## Running Tests

### Individual Service Tests
```bash
# Run tests for a specific service
mvn test -f inventory-service/pom.xml
mvn test -f transaction-service/pom.xml
mvn test -f personnel-service/pom.xml
mvn test -f store-service/pom.xml
```

### All Tests
```bash
# Run all tests
./run-all-tests.cmd
```

## Test Categories

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test service interactions
- **API Tests**: Test REST endpoints
- **Service Tests**: Test business logic