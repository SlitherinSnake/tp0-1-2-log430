To insert my employes.sql
docker exec -i postgres-db psql -U admin -d magasin < .sql/employes.sql
result expected: INSERT 0 4
To verify
docker exec -it postgres-db psql -U admin -d magasin -c "SELECT * FROM employes;"

To insert my produits.sql
docker exec -i postgres-db psql -U admin -d magasin < .sql/produits.sql
result expected: INSERT 0 12
To verify
docker exec -it postgres-db psql -U admin -d magasin -c "SELECT * FROM produits;"

Si doublon par accident, delete
docker exec -it postgres-db psql -U admin -d magasin -c "DELETE FROM produits;"
result expected: DELETE 16
Reinitialiser les id <- Sinon sa sera 17,18 par exemple
docker exec -it postgres-db psql -U admin -d magasin -c "ALTER SEQUENCE produits_id_seq RESTART WITH 1;"
result expected: ALTER SEQUENCE
Après tu réinsert le sql
docker exec -i postgres-db psql -U admin -d magasin < .sql/produits.sql
result expected: INSERT 0 12
Finalement, tu vérifie
docker exec -it postgres-db psql -U admin -d magasin -c "SELECT * FROM produits;"
result expected: (12 rows)



