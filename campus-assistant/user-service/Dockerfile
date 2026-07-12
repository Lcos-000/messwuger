FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY campusswu-core campusswu-core
COPY campusswu-mybatis campusswu-mybatis
COPY campusswu-redis campusswu-redis
COPY campusswu-web campusswu-web
COPY campusswu-gateway campusswu-gateway
COPY user-service user-service
COPY course-service course-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/user-service/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
