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
    ('Fruits','Banane',0.99,96),
    ('Fruits','Fraise',1.99,42),
    ('Fruits','Melon',3.99,22),
    ('Legumes','Tomate',2.99,50),
    ('Legumes','Concombre',0.99,72),
    ('Legumes','Laitue',1.99,62),
    ('Electroniques','TV',100.99,19),
    ('Electroniques','PS5',300.99,42),
    ('Electroniques','GTAVI',120.99,38),
    ('Vetements','Chandail',8.99,50),
    ('Vetements','Pantalon',10.99,50),
    ('Vetements','Manteau',250.99,90);
