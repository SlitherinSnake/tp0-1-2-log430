# ----------------------------
# Step 1: Build the project with Maven
# ----------------------------

# Use a Maven image with JDK 21 already installed
FROM maven:3.9.5-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /build

# Copy Maven config file
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the project and skip tests
RUN mvn clean package -DskipTests


# ----------------------------
# Step 2: Use a smaller image to run the app
# ----------------------------

# Use a lightweight Java image
FROM eclipse-temurin:21-jdk-slim

# Set the working directory for the running app
WORKDIR /app

# Copy .jar file built in the first step
COPY --from=build /build/target/*.jar app.jar

# Run app when the container starts
CMD ["java", "-jar", "app.jar"]
