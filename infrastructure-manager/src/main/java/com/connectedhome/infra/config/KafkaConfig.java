package com.connectedhome.infra.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {
    @Bean
    @ConditionalOnProperty(name = "app.kafka.create-topics", havingValue = "true", matchIfMissing = true)
    NewTopic rawMetricsTopic(@Value("${app.kafka.topics.raw-metrics}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.create-topics", havingValue = "true", matchIfMissing = true)
    NewTopic validatedMetricsTopic(@Value("${app.kafka.topics.validated-metrics}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.create-topics", havingValue = "true", matchIfMissing = true)
    NewTopic quarantineTopic(@Value("${app.kafka.topics.quarantine}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.create-topics", havingValue = "true", matchIfMissing = true)
    NewTopic deadLetterTopic(@Value("${app.kafka.topics.dead-letter}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.topics.dead-letter}") String deadLetterTopic) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(deadLetterTopic, record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 2L));
    }
}
