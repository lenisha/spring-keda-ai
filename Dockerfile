FROM azul/zulu-openjdk-alpine:11
VOLUME /tmp
EXPOSE 8080

COPY target/amqp-0.0.1-SNAPSHOT.jar app.jar
COPY applicationinsights-agent-3.0.0.jar  applicationinsights-agent-3.0.0.jar

ENTRYPOINT java -javaagent:/applicationinsights-agent-3.0.0.jar -Djava.security.egd=file:/dev/./urandom -jar /app.jar