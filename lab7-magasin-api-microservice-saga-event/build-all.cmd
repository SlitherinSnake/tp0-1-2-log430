@echo off
echo Building all microservices...

echo Building discovery-server...
cd discovery-server
call mvn clean package -DskipTests
cd ..

echo Building api-gateway...
cd api-gateway
call mvn clean package -DskipTests
cd ..

echo Building frontend-service...
cd frontend-service
call mvn clean package -DskipTests
cd ..

echo Building inventory-service...
cd inventory-service
call mvn clean package -DskipTests
cd ..

echo Building transaction-service...
cd transaction-service
call mvn clean package -DskipTests
cd ..

echo Building store-service...
cd store-service
call mvn clean package -DskipTests
cd ..

echo Building personnel-service...
cd personnel-service
call mvn clean package -DskipTests
cd ..

echo All microservices built successfully!
echo You can now run: docker-compose up --build
