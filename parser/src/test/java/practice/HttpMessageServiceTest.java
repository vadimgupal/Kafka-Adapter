package practice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpMessageServiceTest {

    private WireMockServer wireMockServer;
    private HttpMessageService httpMessageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(5_000);

        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .build();

        httpMessageService = new HttpMessageService(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void buildHttpCallString_shouldBuildUrlWithPathVariablesAndQueryParams() {
        HttpRequestMessage message = new HttpRequestMessage();
        message.setType(HttpMethod.GET);
        message.setUrl("http://localhost:" + wireMockServer.port() + "/books/{id}");
        message.setPathVariables(Map.of("id", "15"));
        message.setQueryParams(Map.of("author", "king", "name", "it"));

        String call = httpMessageService.buildHttpCallString(message);

        assertTrue(call.startsWith("GET http://localhost:" + wireMockServer.port() + "/books/15"));
        assertTrue(call.contains("author=king"));
        assertTrue(call.contains("name=it"));
    }

    @Test
    void process_shouldSendGetRequestWithHeadersAndQueryParams() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/42"))
                .withQueryParam("author", equalTo("king"))
                .withHeader("X-Request-Id", equalTo("req-123"))
                .willReturn(ok("book-response")));

        HttpRequestMessage message = new HttpRequestMessage();
        message.setType(HttpMethod.GET);
        message.setUrl("http://localhost:" + wireMockServer.port() + "/books/{id}");
        message.setPathVariables(Map.of("id", "42"));
        message.setQueryParams(Map.of("author", "king"));
        message.setHeaders(Map.of("X-Request-Id", "req-123"));

        String response = httpMessageService.process(message);

        assertEquals("book-response", response);

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/books/42"))
                .withQueryParam("author", equalTo("king"))
                .withHeader("X-Request-Id", equalTo("req-123")));
    }

    @Test
    void process_shouldSendPostRequestWithBody() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/books"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.title", equalTo("It")))
                .willReturn(ok("created")));

        HttpRequestMessage message = new HttpRequestMessage();
        message.setType(HttpMethod.POST);
        message.setUrl("http://localhost:" + wireMockServer.port() + "/books");
        message.setHeaders(Map.of("Content-Type", "application/json"));
        message.setBody(objectMapper.readTree("""
                {
                  "title": "It"
                }
                """));

        String response = httpMessageService.process(message);

        assertEquals("created", response);

        wireMockServer.verify(postRequestedFor(urlEqualTo("/books"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.title", equalTo("It"))));
    }

    private void injectRestClient(HttpMessageService service, RestClient restClient) {
        try {
            var field = HttpMessageService.class.getDeclaredField("restClient");
            field.setAccessible(true);
            field.set(service, restClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}