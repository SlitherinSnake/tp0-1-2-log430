FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/transaction-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Install curl for health checks
RUN apk add --no-cache curl

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
