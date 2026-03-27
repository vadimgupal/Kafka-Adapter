package practice;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"http-requests", "http-requests-dlt"})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaRequestListenerIntegrationTest {

    private WireMockServer wireMockServer;

    @Autowired
    private KafkaTemplate<String, byte[]> bytesKafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeAll
    void beforeAll() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();

        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers"));
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "parser-group-it");
        registry.add("spring.http.client.factory", () -> "simple");
    }

    @Test
    void shouldConsumeKafkaMessageAndInvokeHttpEndpoint() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/100"))
                .withQueryParam("author", equalTo("king"))
                .withHeader("X-Test", equalTo("yes"))
                .willReturn(ok("success-from-wiremock")));

        String json = """
                {
                  "type": "GET",
                  "url": "http://localhost:%d/books/{id}",
                  "headers": {
                    "X-Test": "yes"
                  },
                  "queryParams": {
                    "author": "king"
                  },
                  "pathVariables": {
                    "id": "100"
                  }
                }
                """.formatted(wireMockServer.port());

        bytesKafkaTemplate.send("http-requests", json.getBytes(StandardCharsets.UTF_8));
        bytesKafkaTemplate.flush();

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() ->
                        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/books/100"))
                                .withQueryParam("author", equalTo("king"))
                                .withHeader("X-Test", equalTo("yes")))
                );
    }

    @Test
    void shouldConsumePostMessageAndInvokeHttpEndpointWithBody() {
        wireMockServer.stubFor(post(urlEqualTo("/books"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.title", equalTo("Kafka in Action")))
                .willReturn(ok("created")));

        String json = """
                {
                  "type": "POST",
                  "url": "http://localhost:%d/books",
                  "headers": {
                    "Content-Type": "application/json"
                  },
                  "body": {
                    "title": "Kafka in Action"
                  }
                }
                """.formatted(wireMockServer.port());

        bytesKafkaTemplate.send("http-requests", json.getBytes(StandardCharsets.UTF_8));
        bytesKafkaTemplate.flush();

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() ->
                        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/books"))
                                .withHeader("Content-Type", containing("application/json"))
                                .withRequestBody(matchingJsonPath("$.title", equalTo("Kafka in Action"))))
                );
    }
}