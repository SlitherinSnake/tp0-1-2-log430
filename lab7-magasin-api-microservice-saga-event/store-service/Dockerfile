FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build

# Copy and build event-infrastructure first
COPY event-infrastructure/pom.xml ./event-infrastructure/
COPY event-infrastructure/src ./event-infrastructure/src/
WORKDIR /build/event-infrastructure
RUN mvn clean install -DskipTests

# Now build the store service
WORKDIR /build
COPY store-service/pom.xml .
COPY store-service/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /build/target/store-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
RUN apk add --no-cache curl
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
