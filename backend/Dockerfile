# Build stage
FROM gradle:8.14-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle clean bootJar -x test --no-daemon --refresh-dependencies

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y netcat-openbsd
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "/app/app.jar"]
COPY wait-for-it.sh .
RUN chmod +x ./wait-for-it.sh
ENTRYPOINT ["/app/wait-for-it.sh", "mysql", "3306", "java", "-jar", "/app/app.jar"]
