-- Create separate databases for each microservice
CREATE DATABASE gateway_db;
CREATE DATABASE inventory_db;
CREATE DATABASE transaction_db;
CREATE DATABASE store_db;
CREATE DATABASE personnel_db;

-- Grant privileges to the magasin user
GRANT ALL PRIVILEGES ON DATABASE gateway_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE store_db TO magasin;
GRANT ALL PRIVILEGES ON DATABASE personnel_db TO magasin;
