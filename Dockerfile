#
# Build stage
#
# Sử dụng base image chứa Java 20 và Maven
FROM maven:3.8.3-openjdk-17 AS build

COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
# Chạy ứng dụng trên base image chứa Java 20
FROM openjdk:17-jdk-alpine

COPY --from=build /target/prettyshopbe-0.0.1-SNAPSHOT.jar prettyshopbe.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","prettyshopbe.jar"]