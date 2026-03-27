package practice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaRequestListener {
    private final HttpMessageService httpMessageService;

    public KafkaRequestListener(HttpMessageService service) {
        this.httpMessageService = service;
    }

    @KafkaListener(topics = "http-requests",
            containerFactory = "httpRequestKafkaListenerContainerFactory")
    public void listen(HttpRequestMessage message,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            String response = httpMessageService.process(message);
            log.info("HTTP request processed successfully: topic={}, partition={}, offset={}, response={}",
                    topic, partition, offset, response);
        } catch (Exception ex) {
            log.error("HTTP request processing failed: topic={}, partition={}, offset={}, call={}",
                    topic, partition, offset, httpMessageService.buildHttpCallString(message), ex);
            throw ex;
        }
    }
}
