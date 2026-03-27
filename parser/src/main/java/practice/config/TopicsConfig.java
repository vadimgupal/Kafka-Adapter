package practice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicsConfig {
    @Bean
    public NewTopic httpRequestsTopic() {
        return TopicBuilder.name("http-requests")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic httpRequestsDltTopic() {
        return TopicBuilder.name("http-requests-dlt")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
