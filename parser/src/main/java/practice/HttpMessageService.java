package practice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@Service
public class HttpMessageService {
    private RestClient restClient;

    public HttpMessageService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String process(HttpRequestMessage message) {
        RestClient.RequestBodySpec requestSpec = restClient.method(message.getType()).uri(buildURI(message));
        applyHeaders(requestSpec, message.getHeaders());
        Object body = message.getBody();
        String response;

        if(body != null && checkMethod(message.getType())) {
            response = requestSpec.body(body)
                    .retrieve()
                    .body(String.class);
        } else {
            response = requestSpec.retrieve().body(String.class);
        }

        log.info("HTTP call finished successfully: call={}, response={}", buildHttpCallString(message), response);
        return response;
    }

    private URI buildURI(HttpRequestMessage message) {
        String resolveUri = replacePathVariables(message.getUrl(), message.getPathVariables());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resolveUri);

        if(message.getQueryParams() != null) {
            for (var x : message.getQueryParams().entrySet()) {
                builder.queryParam(x.getKey(), x.getValue());
            }
        }

        return builder.build(true).toUri();
    }

    private String replacePathVariables(String url, Map<String, String> pathVariables) {
        String res = url;

        if (pathVariables != null) {
            for (var set : pathVariables.entrySet()) {
                res = res.replace("{" + set.getKey() + "}", set.getValue());
            }
        }

        return res;
    }

    private void applyHeaders(RestClient.RequestBodySpec spec, Map<String, String> heades) {
        if(heades != null) {
            for (var x : heades.entrySet()) {
                spec.header(x.getKey(), x.getValue());
            }
        }
    }

    private boolean checkMethod(HttpMethod type) {
        return type == HttpMethod.PATCH ||
                type == HttpMethod.PUT ||
                type == HttpMethod.POST;
    }

    public String buildHttpCallString(HttpRequestMessage message) {
        URI uri = buildURI(message);
        return message.getType() + " " + uri;
    }
}
