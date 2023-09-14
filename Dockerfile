FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/spring.jar .

CMD ["java", "-jar", "spring.jar"]
