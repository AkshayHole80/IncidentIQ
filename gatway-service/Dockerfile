# Stage 1 - Build

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -B

 # Stage 2 - Runtime

FROM eclipse-temurin:21-jre

LABEL maintainer="Akshay Hole"

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]