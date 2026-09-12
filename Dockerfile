# Build the Next.js application as a static site. In production it is served by
# Spring Boot, so browser API calls can use the same Render origin.
FROM node:20-bookworm-slim AS frontend-build
WORKDIR /workspace/frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
# An empty value is intentional: the production browser calls /api on its own origin.
ENV NEXT_PUBLIC_API_URL=
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# Compile the Spring Boot application and embed the exported frontend in its
# classpath static resources.
FROM maven:3.9.11-eclipse-temurin-25 AS backend-build
WORKDIR /workspace/backend

COPY backend/pom.xml ./
RUN mvn --batch-mode dependency:go-offline

COPY backend/src ./src
COPY --from=frontend-build /workspace/frontend/out ./src/main/resources/static
RUN mvn --batch-mode clean package -DskipTests

# Render supplies PORT at runtime. The application reads it from application.yml.
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=backend-build /workspace/backend/target/*.jar app.jar
EXPOSE 8080

CMD ["sh", "-c", "exec java -jar /app/app.jar --server.port=${PORT:-8080}"]
