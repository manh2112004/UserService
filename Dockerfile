FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar /app/
RUN rm -f /app/original-*.jar && mv /app/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
