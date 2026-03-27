package practice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class HttpRequestMessage {
    private HttpMethod type;
    private String url;
    private Object  body;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Map<String, String> pathVariables;

    public HttpRequestMessage(HttpMethod type,
                              String url,
                              JsonNode body,
                              Map<String, String> headers){
        this.type = type;
        this.url = url;
        this.body = body;
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "HttpRequestMessage{" +
                "method='" + type + '\'' +
                ", url='" + url + '\'' +
                ", body=" + body +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", pathVariables=" + pathVariables +
                '}';
    }
}
