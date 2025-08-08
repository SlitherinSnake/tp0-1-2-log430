@echo off
REM Test script for comprehensive observability validation
REM Tests metrics collection, alerting, and distributed tracing

echo ===============================================
echo COMPREHENSIVE OBSERVABILITY VALIDATION TEST
echo ===============================================

REM Function to check service health
:check_service_health
set service_name=%1
set port=%2
echo [INFO] Checking %service_name% health on port %port%...

curl -f -s "http://localhost:%port%/actuator/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] %service_name% is healthy
) else (
    echo [ERROR] %service_name% is not responding
)
goto :eof

REM Function to check Prometheus metrics
:check_prometheus_metrics
set service_name=%1
set port=%2
set metric_name=%3
echo [INFO] Checking %metric_name% metrics for %service_name%...

curl -s "http://localhost:%port%/actuator/prometheus" | findstr /c:"%metric_name%" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] %metric_name% metrics found for %service_name%
) else (
    echo [ERROR] %metric_name% metrics not found for %service_name%
)
goto :eof

REM Function to test event flow with tracing
:test_event_flow_tracing
echo [INFO] Testing event flow with distributed tracing...

set transaction_data={"customerId":"test-customer-001","items":[{"productId":"test-product-001","quantity":2,"price":99.99}],"totalAmount":199.98}

echo [INFO] Creating test transaction...
curl -s -X POST -H "Content-Type: application/json" -d "%transaction_data%" "http://localhost:8081/api/transactions" >response.tmp 2>nul

if %errorlevel% equ 0 (
    echo [SUCCESS] Test transaction created successfully
    type response.tmp
    echo.
    
    echo [INFO] Waiting 10 seconds for saga completion...
    timeout /t 10 /nobreak >nul
    
    echo [INFO] Checking for trace completion logs...
    docker logs lab7-magasin-api-microservice-saga-event-transaction-service-1 2>nul | findstr "TRACE_COMPLETED" >nul
    if %errorlevel% equ 0 (
        echo [SUCCESS] Distributed tracing is working - trace completion logged
    ) else (
        echo [WARNING] No trace completion logs found yet
    )
) else (
    echo [ERROR] Failed to create test transaction
)

if exist response.tmp del response.tmp
goto :eof

REM Function to validate Grafana dashboards
:validate_grafana_dashboards
echo [INFO] Validating Grafana dashboards...

curl -f -s "http://localhost:3000/api/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Grafana is accessible
    
    echo [INFO] Checking available dashboards...
    curl -s "http://admin:admin@localhost:3000/api/search?type=dash-db" >dashboards.tmp 2>nul
    
    findstr /c:"Event-Driven Architecture" dashboards.tmp >nul 2>&1
    if %errorlevel% equ 0 (
        echo [SUCCESS] Event-Driven Architecture dashboard found
    ) else (
        echo [WARNING] Event-Driven Architecture dashboard not found
    )
    
    findstr /c:"Distributed Tracing" dashboards.tmp >nul 2>&1
    if %errorlevel% equ 0 (
        echo [SUCCESS] Distributed Tracing dashboard found
    ) else (
        echo [WARNING] Distributed Tracing dashboard not found
    )
    
    if exist dashboards.tmp del dashboards.tmp
) else (
    echo [ERROR] Grafana is not accessible
)
goto :eof

REM Function to check Prometheus alerting rules
:check_prometheus_alerts
echo [INFO] Checking Prometheus alerting rules...

curl -f -s "http://localhost:9090/api/v1/rules" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Prometheus rules endpoint accessible
    
    curl -s "http://localhost:9090/api/v1/rules" >rules.tmp 2>nul
    
    findstr /c:"EventPublishingFailureRate" rules.tmp >nul 2>&1
    if %errorlevel% equ 0 (
        echo [SUCCESS] EventPublishingFailureRate alert rule found
    ) else (
        echo [WARNING] EventPublishingFailureRate alert rule not found
    )
    
    findstr /c:"SagaCoordinationFailures" rules.tmp >nul 2>&1
    if %errorlevel% equ 0 (
        echo [SUCCESS] SagaCoordinationFailures alert rule found
    ) else (
        echo [WARNING] SagaCoordinationFailures alert rule not found
    )
    
    if exist rules.tmp del rules.tmp
) else (
    echo [ERROR] Prometheus rules endpoint not accessible
)
goto :eof

REM Main test execution
echo.
echo [INFO] Starting comprehensive observability validation...
echo.

REM 1. Service Health Checks
echo 1. SERVICE HEALTH CHECKS
echo ========================
call :check_service_health "transaction-service" "8081"
call :check_service_health "payment-service" "8082"
call :check_service_health "inventory-service" "8083"
call :check_service_health "store-service" "8084"
call :check_service_health "prometheus" "9090"
call :check_service_health "grafana" "3000"
echo.

REM 2. Metrics Collection Tests
echo 2. METRICS COLLECTION TESTS
echo ============================
call :check_prometheus_metrics "transaction-service" "8081" "event_published_total"
call :check_prometheus_metrics "transaction-service" "8081" "event_consumed_total"
call :check_prometheus_metrics "payment-service" "8082" "event_published_total"
call :check_prometheus_metrics "payment-service" "8082" "event_consumed_total"
call :check_prometheus_metrics "inventory-service" "8083" "event_published_total"
call :check_prometheus_metrics "inventory-service" "8083" "event_consumed_total"
echo.

REM 3. Distributed Tracing Tests
echo 3. DISTRIBUTED TRACING TESTS
echo =============================
call :test_event_flow_tracing
echo.

REM 4. Prometheus Alerting Rules
echo 4. PROMETHEUS ALERTING RULES
echo =============================
call :check_prometheus_alerts
echo.

REM 5. Grafana Dashboard Validation
echo 5. GRAFANA DASHBOARD VALIDATION
echo ================================
call :validate_grafana_dashboards
echo.

REM Summary
echo TEST SUMMARY
echo ============
echo [INFO] Observability implementation validation completed
echo [INFO] Check Grafana dashboards at: http://localhost:3000
echo [INFO] Check Prometheus at: http://localhost:9090
echo [INFO] Monitor application logs for distributed tracing output
echo.

pause
