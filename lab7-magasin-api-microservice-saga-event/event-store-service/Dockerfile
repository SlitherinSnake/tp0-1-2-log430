FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/event-store-service-1.0.0.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]