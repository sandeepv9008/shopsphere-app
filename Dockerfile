# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy dependency metadata first to improve Docker layer caching.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy application source and package with tests enabled.
COPY src ./src
RUN mvn -B clean verify && test -f target/shopsphere.jar

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --create-home --uid 10001 appuser

COPY --from=builder --chown=10001:10001 /app/target/shopsphere.jar /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]