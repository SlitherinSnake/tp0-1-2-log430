@echo off
echo Cleaning all microservices...

echo Cleaning discovery-server...
cd discovery-server
call mvn clean -DskipTests
cd ..

echo Cleaning api-gateway...
cd api-gateway
call mvn clean -DskipTests
cd ..

echo Cleaning frontend-service...
cd frontend-service
call mvn clean -DskipTests
cd ..

echo Cleaning inventory-service...
cd inventory-service
call mvn clean -DskipTests
cd ..

echo Cleaning transaction-service...
cd transaction-service
call mvn clean -DskipTests
cd ..

echo Cleaning store-service...
cd store-service
call mvn clean -DskipTests
cd ..

echo Cleaning personnel-service...
cd personnel-service
call mvn clean -DskipTests
cd ..

echo All microservices cleaned successfully!
