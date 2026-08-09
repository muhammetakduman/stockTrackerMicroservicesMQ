# ── Build Stage ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Cache Maven dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Build the application (skip tests – tests are run separately)
COPY src src
RUN ./mvnw package -DskipTests -q

# ── Runtime Stage ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat jar
COPY --from=build /workspace/target/*.jar app.jar

# Fix ownership
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Use exec form for proper signal handling
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]

