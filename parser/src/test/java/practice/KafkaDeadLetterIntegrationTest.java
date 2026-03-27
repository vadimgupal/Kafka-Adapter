package practice;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"http-requests", "http-requests-dlt"})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaDeadLetterIntegrationTest {

    @Autowired
    private KafkaTemplate<String, byte[]> bytesKafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers"));
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "parser-group-dlt-it");
        registry.add("spring.http.client.factory", () -> "simple");
    }

    @BeforeEach
    void waitForListenerAssignment() {
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @Test
    void shouldSendInvalidJsonToDlt() {
        byte[] invalidPayload = """
                {
                  "type": "GET",
                  "url": "http://localhost:8081/books",
                }
                """.getBytes();

        bytesKafkaTemplate.send("http-requests", invalidPayload);
        bytesKafkaTemplate.flush();

        Map<String, Object> props =
                KafkaTestUtils.consumerProps("dlt-consumer", "true", embeddedKafkaBroker);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (Consumer<String, String> consumer =
                     new org.apache.kafka.clients.consumer.KafkaConsumer<>(props)) {

            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "http-requests-dlt");

            ConsumerRecord<String, String> record =
                    KafkaTestUtils.getSingleRecord(consumer, "http-requests-dlt", Duration.ofSeconds(10));

            assertNotNull(record);
            assertTrue(record.value().contains("\"url\": \"http://localhost:8081/books\""));
        }
    }
}