FROM openjdk:11-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} widget-service-1.0.0.jar
ENTRYPOINT ["java","-jar","/widget-service-1.0.0.jar"]