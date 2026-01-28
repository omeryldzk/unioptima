# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the protobuf definitions from the parent directory
# This requires the build context to be the project root
COPY proto /app/proto

# Copy the backend-service project files
COPY backend-service/pom.xml /app/backend-service/
COPY backend-service/src /app/backend-service/src

# Build the application
WORKDIR /app/backend-service
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR file from the build stage
COPY --from=build /app/backend-service/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
