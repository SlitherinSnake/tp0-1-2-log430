# =============================================
# Dockerfile – Spring Boot Web (Multi-stage)
#
# Build image:
# docker build -t magasin-web-app .
#
# Run container:
# docker run --rm -p 8080:8080 magasin-web-app
# =============================================

# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copier tout le projet (src + pom + mvnw)
COPY . .

# Donner les droits si le projet est sur Unix
RUN chmod +x mvnw

# Compiler et empaqueter l'application Spring Boot
RUN ./mvnw clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copier le .jar généré (nom exact à adapter si différent)
COPY --from=build /app/target/*SNAPSHOT.jar app.jar

# Exposer le port de l'application Spring Boot
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
