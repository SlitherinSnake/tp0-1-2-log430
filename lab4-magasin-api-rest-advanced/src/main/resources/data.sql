-- =============================================
-- DDD Architecture Database Schema
-- =============================================

-- Clean up old tables first
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS personnel_roles CASCADE;
DROP TABLE IF EXISTS transaction_items CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS store_inventory CASCADE;
DROP TABLE IF EXISTS inventory_items CASCADE;
DROP TABLE IF EXISTS personnel CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS stores CASCADE;

-- Legacy table cleanup
DROP TABLE IF EXISTS retour_produit CASCADE;
DROP TABLE IF EXISTS retour CASCADE;
DROP TABLE IF EXISTS vente_produit CASCADE;
DROP TABLE IF EXISTS vente CASCADE;
DROP TABLE IF EXISTS stock_magasin CASCADE;
DROP TABLE IF EXISTS stock_central CASCADE;
DROP TABLE IF EXISTS produits CASCADE;
DROP TABLE IF EXISTS employes CASCADE;
DROP TABLE IF EXISTS magasin CASCADE;

-- =============================================
-- Create new DDD-aligned tables
-- =============================================

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Personnel table (unified Employee and User)
CREATE TABLE IF NOT EXISTS personnel (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    identifiant VARCHAR(50) NOT NULL UNIQUE,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Personnel-Roles junction table
CREATE TABLE IF NOT EXISTS personnel_roles (
    personnel_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (personnel_id, role_id),
    FOREIGN KEY (personnel_id) REFERENCES personnel(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Stores table
CREATE TABLE IF NOT EXISTS stores (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    quartier VARCHAR(100) NOT NULL,
    adresse VARCHAR(255),
    telephone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Inventory Items table (unified Product and Stock)
CREATE TABLE IF NOT EXISTS inventory_items (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    categorie VARCHAR(50) NOT NULL,
    prix DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    stock_central INTEGER NOT NULL DEFAULT 0,
    stock_minimum INTEGER DEFAULT 0,
    date_derniere_maj DATE,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Store Inventory table (store-specific stock)
CREATE TABLE IF NOT EXISTS store_inventory (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    quantite_locale INTEGER NOT NULL DEFAULT 0,
    quantite_demandee INTEGER,
    date_demande DATE,
    date_derniere_maj DATE,
    statut_demande VARCHAR(20) NOT NULL DEFAULT 'AUCUNE',
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    UNIQUE(inventory_item_id, store_id)
);

-- Transactions table (unified Sales and Returns)
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    type_transaction VARCHAR(10) NOT NULL CHECK (type_transaction IN ('VENTE', 'RETOUR')),
    date_transaction DATE NOT NULL,
    montant_total DECIMAL(10,2) DEFAULT 0.00,
    personnel_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    transaction_originale_id BIGINT,
    motif_retour VARCHAR(255),
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_COURS' CHECK (statut IN ('EN_COURS', 'COMPLETEE', 'ANNULEE')),
    FOREIGN KEY (personnel_id) REFERENCES personnel(id),
    FOREIGN KEY (store_id) REFERENCES stores(id),
    FOREIGN KEY (transaction_originale_id) REFERENCES transactions(id)
);

-- Transaction Items table
CREATE TABLE IF NOT EXISTS transaction_items (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    inventory_item_id BIGINT NOT NULL,
    quantite INTEGER NOT NULL,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    sous_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

-- =============================================
-- Insert initial data
-- =============================================

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ROLE_ADMIN', 'Administrateur système avec tous les privilèges'),
    ('ROLE_MANAGER', 'Gestionnaire de magasin'),
    ('ROLE_EMPLOYEE', 'Employé de magasin'),
    ('ROLE_VIEWER', 'Consultation uniquement');

-- Insert sample stores
INSERT INTO stores (nom, quartier, adresse, telephone) VALUES 
    ('Magasin Centre-Ville', 'Centre-Ville', '123 Rue Principale', '514-555-0101'),
    ('Magasin Nord', 'Quartier Nord', '456 Avenue Nord', '514-555-0102'),
    ('Magasin Sud', 'Quartier Sud', '789 Boulevard Sud', '514-555-0103');

-- Insert sample personnel
INSERT INTO personnel (nom, identifiant, username, password) VALUES 
    ('Jean Dubois', 'EMP001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1VQ2jhqKNjbq1JkJzJHPqCKLI2W8bHC'), -- password: admin
    ('Marie Martin', 'EMP002', 'manager', '$2a$10$N9qo8uLOickgx2ZMRZoMye1VQ2jhqKNjbq1JkJzJHPqCKLI2W8bHC'), -- password: admin
    ('Pierre Tremblay', 'EMP003', 'employee', '$2a$10$N9qo8uLOickgx2ZMRZoMye1VQ2jhqKNjbq1JkJzJHPqCKLI2W8bHC'), -- password: admin
    ('Client Démo', 'CLI001', 'client', '$2a$10$u1b6QwQwQwQwQwQwQwQwQeQwQwQwQwQwQwQwQwQwQwQwQwQw'); -- password: client123

-- Add CLIENT role if not present
INSERT INTO roles (name, description) VALUES ('ROLE_CLIENT', 'Client du magasin') ON CONFLICT (name) DO NOTHING;

-- Assign roles to personnel
INSERT INTO personnel_roles (personnel_id, role_id) VALUES 
    (1, 1), -- Jean Dubois -> ADMIN
    (2, 2), -- Marie Martin -> MANAGER
    (3, 3), -- Pierre Tremblay -> EMPLOYEE
    (4, 5); -- Client Démo -> CLIENT

-- Insert sample inventory items
INSERT INTO inventory_items (nom, categorie, prix, description, stock_central, stock_minimum, date_derniere_maj) VALUES 
    ('Laptop Dell Inspiron', 'Electronique', 799.99, 'Ordinateur portable Dell Inspiron 15 pouces', 25, 5, CURRENT_DATE),
    ('iPhone 14', 'Electronique', 999.99, 'Smartphone Apple iPhone 14 128GB', 15, 3, CURRENT_DATE),
    ('Chaise de Bureau', 'Mobilier', 149.99, 'Chaise de bureau ergonomique avec support lombaire', 40, 10, CURRENT_DATE),
    ('Livre Java Programming', 'Livres', 49.99, 'Guide complet de programmation Java', 100, 20, CURRENT_DATE),
    ('Cafetière Keurig', 'Electromenager', 89.99, 'Cafetière à dosettes Keurig K-Classic', 30, 8, CURRENT_DATE),
    ('Samsung Galaxy S23', 'Electronique', 899.99, 'Smartphone Samsung Galaxy S23 256GB', 20, 4, CURRENT_DATE),
    ('Table de conférence', 'Mobilier', 399.99, 'Grande table de réunion 10 places', 8, 2, CURRENT_DATE),
    ('Aspirateur Dyson V11', 'Electromenager', 599.99, 'Aspirateur sans fil Dyson V11 Absolute', 12, 3, CURRENT_DATE),
    ('Casque Bose QC45', 'Electronique', 349.99, 'Casque audio sans fil à réduction de bruit', 18, 5, CURRENT_DATE),
    ('Imprimante HP LaserJet', 'Electronique', 229.99, 'Imprimante laser monochrome HP', 14, 3, CURRENT_DATE),
    ('Bureau assis-debout', 'Mobilier', 299.99, 'Bureau réglable en hauteur électrique', 10, 2, CURRENT_DATE),
    ('Livre Python Avancé', 'Livres', 59.99, 'Techniques avancées de programmation Python', 80, 15, CURRENT_DATE),
    ('Grille-pain Moulinex', 'Electromenager', 39.99, 'Grille-pain 2 fentes inox', 25, 5, CURRENT_DATE),
    ('Fauteuil ergonomique', 'Mobilier', 249.99, 'Fauteuil de bureau haut de gamme', 16, 4, CURRENT_DATE),
    ('Tablette iPad Air', 'Electronique', 699.99, 'Apple iPad Air 10.9" 64GB', 13, 3, CURRENT_DATE);

-- Insert store inventory (distribute some inventory to stores)
INSERT INTO store_inventory (inventory_item_id, store_id, quantite_locale, date_derniere_maj) VALUES 
    -- Centre-Ville store
    (1, 1, 8, CURRENT_DATE),
    (2, 1, 5, CURRENT_DATE),
    (3, 1, 12, CURRENT_DATE),
    (4, 1, 25, CURRENT_DATE),
    (5, 1, 10, CURRENT_DATE),
    -- Nord store
    (1, 2, 7, CURRENT_DATE),
    (2, 2, 4, CURRENT_DATE),
    (3, 2, 15, CURRENT_DATE),
    (4, 2, 30, CURRENT_DATE),
    (5, 2, 8, CURRENT_DATE),
    -- Sud store
    (1, 3, 6, CURRENT_DATE),
    (2, 3, 3, CURRENT_DATE),
    (3, 3, 10, CURRENT_DATE),
    (4, 3, 20, CURRENT_DATE),
    (5, 3, 12, CURRENT_DATE);

-- Insert more store inventory for new products
INSERT INTO store_inventory (inventory_item_id, store_id, quantite_locale, date_derniere_maj) VALUES 
    (6, 1, 7, CURRENT_DATE), (7, 1, 2, CURRENT_DATE), (8, 1, 4, CURRENT_DATE), (9, 1, 6, CURRENT_DATE), (10, 1, 3, CURRENT_DATE),
    (11, 1, 2, CURRENT_DATE), (12, 1, 10, CURRENT_DATE), (13, 1, 5, CURRENT_DATE), (14, 1, 3, CURRENT_DATE), (15, 1, 2, CURRENT_DATE),
    (6, 2, 5, CURRENT_DATE), (7, 2, 1, CURRENT_DATE), (8, 2, 3, CURRENT_DATE), (9, 2, 4, CURRENT_DATE), (10, 2, 2, CURRENT_DATE),
    (11, 2, 1, CURRENT_DATE), (12, 2, 8, CURRENT_DATE), (13, 2, 3, CURRENT_DATE), (14, 2, 2, CURRENT_DATE), (15, 2, 1, CURRENT_DATE),
    (6, 3, 4, CURRENT_DATE), (7, 3, 1, CURRENT_DATE), (8, 3, 2, CURRENT_DATE), (9, 3, 3, CURRENT_DATE), (10, 3, 1, CURRENT_DATE),
    (11, 3, 1, CURRENT_DATE), (12, 3, 6, CURRENT_DATE), (13, 3, 2, CURRENT_DATE), (14, 3, 1, CURRENT_DATE), (15, 3, 1, CURRENT_DATE);

-- Insert demo transactions for dashboard activity and sales
INSERT INTO transactions (type_transaction, date_transaction, montant_total, personnel_id, store_id, statut) VALUES
    ('VENTE', CURRENT_DATE - INTERVAL '1 day', 1599.98, 2, 1, 'COMPLETEE'),
    ('VENTE', CURRENT_DATE - INTERVAL '2 days', 999.99, 3, 2, 'COMPLETEE'),
    ('VENTE', CURRENT_DATE - INTERVAL '3 days', 149.99, 2, 3, 'COMPLETEE'),
    ('VENTE', CURRENT_DATE, 229.99, 3, 1, 'COMPLETEE'),
    ('VENTE', CURRENT_DATE, 349.99, 2, 2, 'COMPLETEE'),
    ('VENTE', CURRENT_DATE, 49.99, 3, 3, 'COMPLETEE');

-- Insert demo transaction items
INSERT INTO transaction_items (transaction_id, inventory_item_id, quantite, prix_unitaire, sous_total) VALUES
    (1, 2, 1, 999.99, 999.99),
    (1, 1, 1, 599.99, 599.99),
    (2, 2, 1, 999.99, 999.99),
    (3, 3, 1, 149.99, 149.99),
    (4, 10, 1, 229.99, 229.99),
    (5, 9, 1, 349.99, 349.99),
    (6, 4, 1, 49.99, 49.99);

-- =============================================
-- Create indexes for performance
-- =============================================
CREATE INDEX IF NOT EXISTS idx_personnel_username ON personnel(username);
CREATE INDEX IF NOT EXISTS idx_personnel_identifiant ON personnel(identifiant);
CREATE INDEX IF NOT EXISTS idx_inventory_items_categorie ON inventory_items(categorie);
CREATE INDEX IF NOT EXISTS idx_inventory_items_nom ON inventory_items(nom);
CREATE INDEX IF NOT EXISTS idx_store_inventory_store_id ON store_inventory(store_id);
CREATE INDEX IF NOT EXISTS idx_store_inventory_item_id ON store_inventory(inventory_item_id);
CREATE INDEX IF NOT EXISTS idx_transactions_store_id ON transactions(store_id);
CREATE INDEX IF NOT EXISTS idx_transactions_personnel_id ON transactions(personnel_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date_transaction);
CREATE INDEX IF NOT EXISTS idx_transaction_items_transaction_id ON transaction_items(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_items_inventory_id ON transaction_items(inventory_item_id);
