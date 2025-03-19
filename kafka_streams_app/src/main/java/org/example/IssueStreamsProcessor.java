package org.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class IssueStreamsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IssueStreamsProcessor.class);
    private static final String INPUT_TOPIC = "issueRequests";
    private static final String OUTPUT_TOPIC = "validatedrequests";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "issue-streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "host.docker.internal:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "500");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> inputStream = builder.stream(INPUT_TOPIC);

        KStream<String, String> processedStream = inputStream.mapValues(IssueStreamsProcessor::processIssue);

        processedStream.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static String processIssue(String value) {
        try {
            JsonNode jsonNode = objectMapper.readTree(value);
            String issueType = jsonNode.get("issueType").asText();
            String issueMessage = jsonNode.get("issueMessage").asText();

            String adminEmail = getAdminEmail(issueType);
            EmailSender.sendEmail(adminEmail, issueType, issueMessage);
            logger.info("Sent issue email to: " + adminEmail);

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            logger.error("Error processing issue: ", e);
            return null;
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
