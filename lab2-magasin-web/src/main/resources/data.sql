-- Drop dependent tables first to avoid foreign key constraint violations -
DROP TABLE IF EXISTS retour_produit CASCADE;
DROP TABLE IF EXISTS retour CASCADE;
DROP TABLE IF EXISTS vente_produit CASCADE;
DROP TABLE IF EXISTS vente CASCADE;

-- Then drop the base tables
DROP TABLE IF EXISTS produits CASCADE;
DROP TABLE IF EXISTS employes CASCADE;

-- Recreate table employes
CREATE TABLE employes (
    id SERIAL PRIMARY KEY,
    identifiant VARCHAR(50) NOT NULL,
    nom VARCHAR(100) NOT NULL
);

-- Recreate table produits
CREATE TABLE produits (
    id SERIAL PRIMARY KEY,
    categorie VARCHAR(50),
    nom VARCHAR(100) UNIQUE,
    prix DECIMAL(10,2),
    quantite INTEGER
);

-- Insert employes
INSERT INTO employes (identifiant, nom) VALUES 
    ('emp001','Alice'),
    ('emp002','Tom'),
    ('emp003','Paul'),
    ('emp004','Jennifer');

-- Insert produits
INSERT INTO produits (categorie, nom, prix, quantite) VALUES 
    ('Fruits','Banane',0.99,100),
    ('Fruits','Fraise',1.99,50),
    ('Fruits','Melon',3.99,30),
    ('Legumes','Tomate',2.99,50),
    ('Legumes','Concombre',0.99,75),
    ('Legumes','Laitue',1.99,70),
    ('Electroniques','TV',100.99,25),
    ('Electroniques','PS5',300.99,50),
    ('Electroniques','GTAVI',120.99,60),
    ('Vetements','Chandail',8.99,50),
    ('Vetements','Pantalon',10.99,50),
    ('Vetements','Manteau',250.99,90);
