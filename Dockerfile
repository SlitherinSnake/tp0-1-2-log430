# ----------------------------
# Step 1: Build the project with Maven
# ----------------------------

FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /build

# Copy Maven config
COPY pom.xml .

# Copy source files
COPY src ./src

# Package the app (skip tests to speed up build)
RUN mvn clean package -DskipTests


# ----------------------------
# Step 2: Create a lightweight runtime image
# ----------------------------

FROM eclipse-temurin:21-jdk-slim

WORKDIR /app

# Optional: declare the port your app might use in the future
EXPOSE 8080

# Copy the built JAR from the previous stage
COPY --from=build /build/target/*.jar app.jar

# Run the app
CMD ["java", "-jar", "app.jar"]
