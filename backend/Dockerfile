# ============================================================
# Backend Dockerfile — Spring Boot (Java 25)
# Multi-stage build: compile with Maven wrapper, run a slim JAR
# ============================================================

# ---------- Stage 1: Build ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Maven wrapper + pom first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings (mvnw may have CRLF when committed on Windows)
# and make the wrapper executable.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Pre-download dependencies (cached unless pom.xml changes)
RUN ./mvnw -B dependency:go-offline

# Copy sources and build the executable JAR (skip tests for image build)
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:25-jdk
WORKDIR /app

# Copy the built Spring Boot fat JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
