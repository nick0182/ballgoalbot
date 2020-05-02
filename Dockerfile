FROM openjdk:latest

ARG JAR_FILE

COPY ${JAR_FILE} ballgoalbot.jar

VOLUME /log

ENTRYPOINT ["java", "-jar", "/ballgoalbot.jar"]