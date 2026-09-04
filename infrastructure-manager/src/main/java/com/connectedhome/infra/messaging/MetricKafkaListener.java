package com.connectedhome.infra.messaging;

import com.connectedhome.infra.service.MetricProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MetricKafkaListener {
    private final MetricProcessingService processingService;

    public MetricKafkaListener(MetricProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.validated-metrics}",
            groupId = "${app.kafka.groups.processor}")
    public void consume(RawMetricEvent event) {
        processingService.process(event);
    }
}
