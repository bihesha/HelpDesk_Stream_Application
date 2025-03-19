FROM ubuntu:latest
LABEL authors="Manula"

# Use OpenJDK 21 base image
FROM openjdk:21

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled Kafka Streams application JAR file and JMX exporter JAR to the container
COPY ./target/kafka_streams_app-1.0-SNAPSHOT.jar /app/kafka_streams_app-1.0-SNAPSHOT.jar
COPY ./src/main/resources/jmx_prometheus_javaagent-1.0.1.jar /app/jmx_prometheus_javaagent-1.0.1.jar
COPY ./src/main/resources/kafka_streams_app_jmx_config.yaml /app/kafka_streams_app_jmx_config.yaml

# Expose JMX metrics port (e.g., 7070)
EXPOSE 7070

# Run the Kafka Streams application with JMX exporter agent
ENTRYPOINT ["java", "-javaagent:/app/jmx_prometheus_javaagent-1.0.1.jar=7070:/app/kafka_streams_app_jmx_config.yaml", \
     "-Dcom.sun.management.jmxremote", \
     "-Dcom.sun.management.jmxremote.port=9998", \
     "-Dcom.sun.management.jmxremote.rmi.port=9998", \
     "-Dcom.sun.management.jmxremote.authenticate=false", \
     "-Dcom.sun.management.jmxremote.ssl=false", \
     "-Djava.rmi.server.hostname=0.0.0.0", \
     "-Dcom.sun.management.jmxremote.local.only=false", \
     "-jar", "/app/kafka_streams_app-1.0-SNAPSHOT.jar"]
