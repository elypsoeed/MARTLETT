FROM eclipse-temurin:25-jre
WORKDIR /app
COPY build/libs/martlett-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]