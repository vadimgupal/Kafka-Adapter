FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY httpServer/pom.xml httpServer/
COPY parser/pom.xml parser/

COPY httpServer/src httpServer/src
COPY parser/src parser/src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=builder /app/httpServer/target/httpServer-1.0-SNAPSHOT.jar httpServer.jar
COPY --from=builder /app/parser/target/parser-1.0-SNAPSHOT.jar parser.jar

ENTRYPOINT ["sh", "-c", "java -jar /app/$APP.jar"]