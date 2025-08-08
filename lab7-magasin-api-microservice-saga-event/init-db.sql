-- Create separate databases for each microservice
CREATE DATABASE gateway_db;
CREATE DATABASE inventory_db;
CREATE DATABASE transaction_db;
CREATE DATABASE store_db;
CREATE DATABASE payment_db;
CREATE DATABASE personnel_db;
CREATE DATABASE saga_db;
CREATE DATABASE event_store_db;
CREATE DATABASE auditdb;

-- Create audit user
CREATE USER audituser WITH PASSWORD 'auditpass';

-- Grant privileges to the magasin user
GRANT ALL PRIVILEGES ON DATABASE gateway_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE store_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE personnel_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE saga_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE event_store_db TO magasin;

-- Grant privileges to the audit user
GRANT ALL PRIVILEGES ON DATABASE auditdb TO audituser;
