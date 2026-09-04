package com.connectedhome.infra.messaging;

import com.connectedhome.infra.service.IngressRoutingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IngressKafkaListener {
    private final IngressRoutingService routingService;

    public IngressKafkaListener(IngressRoutingService routingService) {
        this.routingService = routingService;
    }

    @KafkaListener(topics = "${app.kafka.topics.raw-metrics}", groupId = "${app.kafka.groups.ingress}")
    public void consume(RawMetricEvent event) {
        routingService.route(event);
    }
}
