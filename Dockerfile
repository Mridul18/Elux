FROM gradle:8.11-jdk17 AS build

WORKDIR /app

# Copy build files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the application with fatJar
RUN gradle buildFatJar --no-daemon -x test

# Runtime stage (ARM64 compatible)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR (shadowJar creates Elux-0.0.1.jar)
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

