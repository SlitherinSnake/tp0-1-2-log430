# Étape 1 : build
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : exécution
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

RUN apk add --no-cache curl

EXPOSE 8765

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8765/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]
