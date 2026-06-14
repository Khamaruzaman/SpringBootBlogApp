# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (separates dependency layer from code layer)
COPY pom.xml .
RUN mvn dependency:resolve -DskipTests

# Copy the source code
COPY src ./src

# Package the application (skipping tests for speed, remove if preferred)
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Copy the built JAR from the first stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]