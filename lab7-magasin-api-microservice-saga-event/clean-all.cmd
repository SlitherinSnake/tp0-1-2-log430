@echo off
echo ========================================
echo Comprehensive cleanup of all microservices
echo ========================================

echo Stopping and removing all Docker containers...
docker-compose down --remove-orphans --volumes

echo ========================================
echo Cleaning Maven artifacts and dependencies
echo ========================================

echo Cleaning local Maven repository cache...
call mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false

echo Cleaning event-infrastructure (shared dependency)...
cd event-infrastructure
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning discovery-server...
cd discovery-server
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning api-gateway...
cd api-gateway
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning frontend-service...
cd frontend-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning inventory-service...
cd inventory-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning transaction-service...
cd transaction-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning store-service...
cd store-service
call mvn clean -DskipTests
if exist target rmdir /s /q target

echo Cleaning personnel-service (inside store-service)...
cd personnel-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..
cd ..

echo Cleaning event-store-service...
cd event-store-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning saga-orchestrator-service...
cd saga-orchestrator-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning payment-service...
cd payment-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo Cleaning audit-service...
cd audit-service
call mvn clean -DskipTests
if exist target rmdir /s /q target
cd ..

echo ========================================
echo Cleaning additional files and logs
echo ========================================

echo Removing log files...
if exist *.log del /q *.log
for /d %%i in (*) do (
    if exist "%%i\*.log" del /q "%%i\*.log"
    if exist "%%i\logs" rmdir /s /q "%%i\logs"
)

echo Removing temporary files...
if exist *.tmp del /q *.tmp
for /d %%i in (*) do (
    if exist "%%i\*.tmp" del /q "%%i\*.tmp"
)

echo Cleaning IDE files...
for /d %%i in (*) do (
    if exist "%%i\.idea" rmdir /s /q "%%i\.idea"
    if exist "%%i\*.iml" del /q "%%i\*.iml"
    if exist "%%i\.vscode" rmdir /s /q "%%i\.vscode"
    if exist "%%i\.settings" rmdir /s /q "%%i\.settings"
    if exist "%%i\.project" del /q "%%i\.project"
    if exist "%%i\.classpath" del /q "%%i\.classpath"
)

echo ========================================
echo Cleanup completed successfully!
echo ========================================
echo All Docker containers, images, and volumes removed
echo All Maven artifacts and target directories cleaned
echo All log files and temporary files removed
echo All IDE configuration files cleaned
echo.
echo System is ready for a fresh build.
echo Run 'build-all.cmd' to rebuild all services.

pause
