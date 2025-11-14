FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY target/eureka-server-*.jar app.jar
RUN apk add --no-cache wget   # CÀI wget
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app.jar"]