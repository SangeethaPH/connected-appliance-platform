package com.connectedhome.appliances.service;

import com.connectedhome.appliances.model.RawVendorMetric;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "simulator.kafka-publisher.enabled", havingValue = "true", matchIfMissing = true)
public class MetricKafkaPublisher {
    private static final Logger log = LoggerFactory.getLogger(MetricKafkaPublisher.class);

    private final KafkaTemplate<String, RawVendorMetric> kafkaTemplate;
    private final String topic;
    private final Counter publishedCounter;
    private final Counter failureCounter;

    public MetricKafkaPublisher(
            KafkaTemplate<String, RawVendorMetric> kafkaTemplate,
            @Value("${simulator.kafka-publisher.topic}") String topic,
            MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.publishedCounter = Counter.builder("simulator.kafka.events.published")
                .description("Metric events acknowledged by Kafka").register(meterRegistry);
        this.failureCounter = Counter.builder("simulator.kafka.events.failed")
                .description("Metric events Kafka failed to acknowledge").register(meterRegistry);
    }

    @EventListener
    public void publish(RawVendorMetric event) {
        String key = event.vendorCode() + ":" + event.externalApplianceId();
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        failureCounter.increment();
                        log.error("Failed to publish metric event {}: {}", event.eventId(), exception.getMessage());
                    } else {
                        publishedCounter.increment();
                        log.debug("Published metric event {} to partition {}", event.eventId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
