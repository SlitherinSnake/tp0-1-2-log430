@echo off
echo ========================================
echo Building all microservices with clean cache
echo ========================================


echo Cleaning Maven cache and compiled artifacts...
call mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false
for /d %%i in (*) do (
    if exist "%%i\target" (
        echo Cleaning %%i target directory...
        rmdir /s /q "%%i\target"
    )
    if exist "%%i\personnel-service\target" (
        echo Cleaning %%i\personnel-service target directory...
        rmdir /s /q "%%i\personnel-service\target"
    )
)

echo ========================================
echo Building in correct dependency order...
echo ========================================

echo [1/9] Building event-infrastructure (shared dependency)...
cd event-infrastructure
call mvn clean install -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build event-infrastructure
    cd ..
    pause
    exit /b 1
)
cd ..

echo [2/9] Building discovery-server...
cd discovery-server
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build discovery-server
    cd ..
    pause
    exit /b 1
)
cd ..

echo [3/9] Building api-gateway...
cd api-gateway
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build api-gateway
    cd ..
    pause
    exit /b 1
)
cd ..

echo [4/9] Building frontend-service...
cd frontend-service
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build frontend-service
    cd ..
    pause
    exit /b 1
)
cd ..

echo [5/9] Building inventory-service...
cd inventory-service
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build inventory-service
    cd ..
    pause
    exit /b 1
)
cd ..

echo [6/9] Building transaction-service...
cd transaction-service
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build transaction-service
    cd ..
    pause
    exit /b 1
)
cd ..

echo [7/9] Building store-service...
cd store-service
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build store-service
    cd ..
    pause
    exit /b 1
)
cd ..

echo [7.1/9] Building personnel-service (inside store-service)...
cd store-service\personnel-service
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build personnel-service
    cd ..\..
    pause
    exit /b 1
)
cd ..\..

echo [8/9] Building event-store-service...
cd event-store-service
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build event-store-service
    cd ..
    pause
    exit /b 1
)
cd ..

echo [9/9] Building saga-orchestrator-service...
cd saga-orchestrator-service
call mvn clean package -DskipTests -U
if %ERRORLEVEL% neq 0 (
    echo Failed to build saga-orchestrator-service
    cd ..
    pause
    exit /b 1
)
cd ..

echo ========================================
echo All microservices built successfully!
echo ========================================
echo Clean build completed with latest dependencies.
echo You can now run: docker-compose up --build --no-cache
echo Or for a complete fresh start: docker-compose up --build --no-cache --force-recreate

pause
