# Stage 1: Build the application
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Cache Gradle wrapper and dependencies layer
COPY gradlew ./
COPY gradle/ ./gradle/
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon

# Copy source code and build
COPY src/ ./src/

# installDist creates the application structure at build/install/diehugos-backend/
RUN ./gradlew installDist --no-daemon

# Isolate the application JAR from the dependency JARs for Docker caching
RUN mkdir /app/deps && \
    mv /app/build/install/diehugos-backend/lib/* /app/deps/ && \
    mv /app/deps/diehugos-backend-1.0.0-SNAPSHOT.jar /app/app.jar

# Stage 2: Create the optimized runtime image
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S ktor && adduser -S ktor -G ktor
USER ktor:ktor

# Layer 1: Dependencies (Cached heavily by Docker)
COPY --from=builder --chown=ktor:ktor /app/deps/ ./lib/

# Layer 2: Application Code (Rebuilt upon source code changes)
COPY --from=builder --chown=ktor:ktor /app/app.jar ./app.jar

EXPOSE 8000

# Execute by defining the classpath to include both the library layer and the app jar
CMD ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-cp", "lib/*:app.jar", "io.ktor.server.netty.EngineMain"]