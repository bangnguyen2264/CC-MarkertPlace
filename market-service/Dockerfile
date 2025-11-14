FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY target/market-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","/app.jar"]