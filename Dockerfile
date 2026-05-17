# ==========================================
# Stage 1: Build the application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the Maven wrapper and pom.xml first
# This allows Docker to cache the downloaded dependencies if pom.xml hasn't changed
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Grant execution permission to the Maven wrapper
RUN chmod +x ./mvnw

# Download dependencies (this step will be cached)
RUN ./mvnw dependency:go-offline -B

# Copy the actual source code
COPY src src

# Build the application into a .war file (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Run the application
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built .war file from the build stage
COPY --from=build /app/target/ROOT.war app.war

# Expose the default Spring Boot port
EXPOSE 8080

# Command to run the application
# Note: Spring Boot .war files are executable with java -jar
ENTRYPOINT ["java", "-jar", "app.war"]
