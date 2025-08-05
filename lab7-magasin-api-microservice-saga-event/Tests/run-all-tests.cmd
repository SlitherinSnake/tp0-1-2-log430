@echo off
echo Starting comprehensive test suite...
echo.

set FAILED_TESTS=0

echo ========================================
echo Running Inventory Service Tests
echo ========================================
cd /d "%~dp0\..\inventory-service"
call mvn test -Dspring.profiles.active=test
if %ERRORLEVEL% neq 0 (
    echo FAILED: Inventory Service Tests
    set /a FAILED_TESTS+=1
) else (
    echo PASSED: Inventory Service Tests
)
echo.

echo ========================================
echo Running Transaction Service Tests
echo ========================================
cd /d "%~dp0\..\transaction-service"
call mvn test -Dspring.profiles.active=test
if %ERRORLEVEL% neq 0 (
    echo FAILED: Transaction Service Tests
    set /a FAILED_TESTS+=1
) else (
    echo PASSED: Transaction Service Tests
)
echo.

echo ========================================
echo Running Personnel Service Tests
echo ========================================
cd /d "%~dp0\..\personnel-service"
call mvn test -Dspring.profiles.active=test
if %ERRORLEVEL% neq 0 (
    echo FAILED: Personnel Service Tests
    set /a FAILED_TESTS+=1
) else (
    echo PASSED: Personnel Service Tests
)
echo.

echo ========================================
echo Running Store Service Tests
echo ========================================
cd /d "%~dp0\..\store-service"
call mvn test -Dspring.profiles.active=test
if %ERRORLEVEL% neq 0 (
    echo FAILED: Store Service Tests
    set /a FAILED_TESTS+=1
) else (
    echo PASSED: Store Service Tests
)
echo.

echo ========================================
echo Running API Gateway Tests
echo ========================================
cd /d "%~dp0\..\api-gateway"
call mvn test -Dspring.profiles.active=test
if %ERRORLEVEL% neq 0 (
    echo FAILED: API Gateway Tests
    set /a FAILED_TESTS+=1
) else (
    echo PASSED: API Gateway Tests
)
echo.

echo ========================================
echo Test Summary
echo ========================================
if %FAILED_TESTS% equ 0 (
    echo All tests PASSED! ✓
    exit /b 0
) else (
    echo %FAILED_TESTS% test suite(s) FAILED! ✗
    exit /b 1
)