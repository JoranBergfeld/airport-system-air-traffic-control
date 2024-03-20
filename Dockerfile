FROM eclipse-temurin:17.0.6_10-jdk-alpine
WORKDIR /app
COPY target/air-traffic-control-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
