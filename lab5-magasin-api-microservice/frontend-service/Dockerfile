FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the jar file
COPY target/frontend-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Install curl for health checks
RUN apk add --no-cache curl

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
