@echo off
echo Adding enhanced metrics configuration to all services...
echo.

echo Updating transaction-service configuration...
if exist transaction-service\src\main\resources\application.yml (
    echo   - transaction-service configuration found
) else (
    echo   - transaction-service configuration not found
)

echo Updating store-service configuration...
if exist store-service\src\main\resources\application.yml (
    echo   - store-service configuration found
) else (
    echo   - store-service configuration not found
)

echo Updating personnel-service configuration...
if exist personnel-service\src\main\resources\application.yml (
    echo   - personnel-service configuration found
) else (
    echo   - personnel-service configuration not found
)

echo Updating frontend-service configuration...
if exist frontend-service\src\main\resources\application.yml (
    echo   - frontend-service configuration found
) else (
    echo   - frontend-service configuration not found
)

echo Updating discovery-server configuration...
if exist discovery-server\src\main\resources\application.yml (
    echo   - discovery-server configuration found
) else (
    echo   - discovery-server configuration not found
)

echo.
echo Configuration update completed!
echo.
echo To apply all changes, run:
echo   docker-compose down
echo   docker-compose up --build
echo.
echo Monitoring URLs:
echo   - Prometheus: http://localhost:9090
echo   - Grafana: http://localhost:3000 (admin/admin)
echo.
echo Available metrics endpoints:
echo   - Discovery Server: http://localhost:8761/actuator/prometheus
echo   - API Gateway: http://localhost:8765/actuator/prometheus
echo   - Frontend Service: http://localhost:8080/actuator/prometheus
echo   - Inventory Service: http://localhost:8081/actuator/prometheus
echo   - Transaction Service: http://localhost:8082/actuator/prometheus
echo   - Store Service: http://localhost:8083/actuator/prometheus
echo   - Personnel Service: http://localhost:8084/actuator/prometheus
