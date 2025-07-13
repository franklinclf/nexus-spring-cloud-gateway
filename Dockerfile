FROM eclipse-temurin:24-alpine

RUN apk add --no-cache curl

WORKDIR /app

ARG JAR_FILE=/build/libs/Gateway-1.0.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]