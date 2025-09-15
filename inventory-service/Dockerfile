FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/inventory-service-0.0.1-SNAPSHOT.jar inventory-service.jar

EXPOSE 8081 9090

ENTRYPOINT ["java", "-jar", "inventory-service.jar"]