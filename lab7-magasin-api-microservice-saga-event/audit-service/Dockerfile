FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build

# Copy and build event-infrastructure first
COPY event-infrastructure/pom.xml ./event-infrastructure/
COPY event-infrastructure/src ./event-infrastructure/src/
WORKDIR /build/event-infrastructure
RUN mvn clean install -DskipTests

# Now build the audit service
WORKDIR /build
COPY audit-service/pom.xml .
COPY audit-service/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

LABEL maintainer="LOG430 Team"
LABEL description="Audit Service for Event-Driven Architecture"

WORKDIR /app

# Copy the JAR file
COPY --from=build /build/target/audit-service-*.jar app.jar

# Install curl and create non-root user for security
RUN apk add --no-cache curl && \
    addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser && \
    mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the port
EXPOSE 8088

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8088/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
