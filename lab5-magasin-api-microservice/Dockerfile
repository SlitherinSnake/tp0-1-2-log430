# ----------------------------
#  Docker - Usage Instructions
# ----------------------------

# To build the image
# docker build -t java-hello-app .

# To run the container (console app)
# docker run --rm java-hello-app

# To run the container (web app on port 8080)
# docker run --rm -p 8080:8080 java-hello-app

# ----------------------------
# Step 1: Build the project with Maven
# ----------------------------

FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /build

# Copy Maven config
COPY pom.xml .

# Copy source files
COPY src ./src

# Package the app
RUN mvn clean package -DskipTests


# ----------------------------
# Step 2: Create a lightweight runtime image
# ----------------------------

FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /build/target/*.jar app.jar

# Run the app
CMD ["java", "-jar", "app.jar"]
