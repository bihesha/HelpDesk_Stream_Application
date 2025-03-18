package org.example;

import com.thulawa.kafka.ThulawaKafkaStreams;
import com.thulawa.kafka.internals.configs.ThulawaConfigs;
import com.thulawa.kafka.internals.suppliers.ThulawaProcessorSupplier;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ThulawaKafkaStreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ThulawaKafkaStreamProcessor.class);
    private static final String INPUT_TOPIC = "issueRequests";
    private static final String OUTPUT_TOPIC = "validatedrequests";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        // Kafka Streams configuration
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "issue-streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "host.docker.internal:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "500");
        props.put(ThulawaConfigs.HIGH_PRIORITY_KEY_LIST, "REGISTRATION_ISSUE, EXAMINATION_ISSUE, PAYMENT_ISSUE");

        // Create the StreamsBuilder
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> inputStream = builder.stream(INPUT_TOPIC);

        // Apply Thulawa processor
        inputStream.process(ThulawaProcessorSupplier.createThulawaProcessorSupplier(new ProcessEach()));

        // Build and start the Kafka Streams application
        ThulawaKafkaStreams streams = new ThulawaKafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    static class ProcessEach implements Processor<String, String, String, String> {
        @Override
        public void process(Record<String, String> record) {
            try {
                JsonNode jsonNode = objectMapper.readTree(record.value());
                String issueType = jsonNode.get("issueType").asText();
                String issueMessage = jsonNode.get("issueMessage").asText();

                String adminEmail = getAdminEmail(issueType);
                EmailSender.sendEmail(adminEmail, issueType, issueMessage);
                logger.info("Sent issue email to: " + adminEmail);
            } catch (Exception e) {
                logger.error("Error processing issue: ", e);
            }
        }
    }

    private static String getAdminEmail(String issueType) {
        return switch (issueType) {
            case "REGISTRATION_ISSUE", "PAYMENT_ISSUE" -> "thulawadmin1@blondmail.com";
            case "EXAMINATION_ISSUE", "MODULE_CONTENT_ISSUE" -> "thulawadmin2@blondmail.com";
            case "CONVOCATION_ISSUE", "REQUEST_DOCUMENTS" -> "thulawadmin3@blondmail.com";
            case "CAMPUS_ENVIRONMENT_ISSUE", "OTHER_ISSUE" -> "thulawadmin4@blondmail.com";
            default -> "general.thulawadmin@blondmail.com";
        };
    }
}
