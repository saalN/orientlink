# ==============================================================================
# Multi-stage Dockerfile for Spring Boot Application
# Optimized for production use with security and performance best practices
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Stage
# Purpose: Compile and package the Spring Boot application
# Uses Maven wrapper to ensure consistent builds
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory for the build
WORKDIR /build

# Copy Maven wrapper and pom.xml first (for better layer caching)
# These files change less frequently than source code
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application (skip tests for faster builds, run tests in CI/CD)
# Creates a JAR file in target/ directory
RUN ./mvnw clean package -DskipTests -B

# Extract layers from the JAR for better layer caching in runtime
# Spring Boot 2.3+ supports layered JARs for optimal Docker layering
RUN java -Djarmode=layertools -jar target/*.jar extract

# ------------------------------------------------------------------------------
# Stage 2: Runtime Stage
# Purpose: Create minimal runtime image with only necessary components
# Uses JRE instead of JDK to reduce image size and attack surface
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadata labels (OCI standard)
LABEL maintainer="salvacode"
LABEL description="OrientLink Spring Boot Application"
LABEL version="0.0.1-SNAPSHOT"

# Install dumb-init for proper signal handling and process reaping
# This ensures graceful shutdowns in containerized environments
RUN apk add --no-cache dumb-init

# Create a non-root user for security (principle of least privilege)
# Running as root is a security risk in production
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy Spring Boot layers from builder stage
# This approach maximizes layer caching since dependencies change less often
COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

# Change ownership of application files to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
# This is documentary - actual port mapping is done with docker run -p
EXPOSE 8080

# Environment variables for configuration
# These can be overridden at runtime
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError" \
    SERVER_PORT=8080

# Health check to monitor application status
# This integrates with Spring Boot Actuator's health endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Use dumb-init to handle signals properly
# JarLauncher is Spring Boot's default launcher for layered JARs
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
