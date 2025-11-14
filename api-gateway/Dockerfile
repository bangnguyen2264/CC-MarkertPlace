FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
RUN apk add --no-cache wget   # CÀI wget
EXPOSE 8222
ENTRYPOINT ["java","-jar","/app.jar"]