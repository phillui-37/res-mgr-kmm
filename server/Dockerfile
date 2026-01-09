# Stage 1: Build the application
ENTRYPOINT ["/app/bin/server"]

EXPOSE 8080

VOLUME /app/data
VOLUME /app/logs
RUN mkdir -p /app/logs /app/data
# Create directory for logs and data

COPY --from=build /app/server/build/install/server /app
WORKDIR /app
FROM eclipse-temurin:21-jre-alpine
# Stage 2: Runtime environment

RUN ./gradlew server:installDist --no-daemon
COPY . .
WORKDIR /app
FROM gradle:8-jdk21 AS build

