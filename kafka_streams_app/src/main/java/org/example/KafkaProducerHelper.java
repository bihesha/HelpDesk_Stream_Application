package org.example;

import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class KafkaProducerHelper {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String OUTPUT_TOPIC = "validatedrequests";

    private static Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    public static void sendToValidatedTopic(String key, String value) {
        Producer<String, String> producer = createProducer();
        ProducerRecord<String, String> record = new ProducerRecord<>(OUTPUT_TOPIC, key, value);
        producer.send(record);
        producer.close();
    }
}