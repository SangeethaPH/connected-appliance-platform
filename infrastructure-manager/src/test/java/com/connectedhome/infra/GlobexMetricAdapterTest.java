package com.connectedhome.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.connectedhome.infra.adapter.GlobexMetricAdapter;
import com.connectedhome.infra.messaging.RawMetricEvent;
import org.junit.jupiter.api.Test;

class GlobexMetricAdapterTest {
    private final GlobexMetricAdapter adapter = new GlobexMetricAdapter();

    @Test
    void convertsGlobexUnitsToCanonicalUnits() {
        RawMetricEvent event = new RawMetricEvent(UUID.randomUUID(), "globex", "GLX-100",
                "REFRIGERATOR", "1.0", Instant.now(),
                Map.of("energy_kw", 0.12, "cabinet_temperature_f", 39.56), Map.of());

        assertThat(adapter.normalize(event))
                .anySatisfy(metric -> {
                    assertThat(metric.name()).isEqualTo("POWER");
                    assertThat(metric.value()).isEqualTo(120.0);
                    assertThat(metric.unit()).isEqualTo("W");
                })
                .anySatisfy(metric -> {
                    assertThat(metric.name()).isEqualTo("INTERNAL_TEMPERATURE");
                    assertThat(metric.value()).isEqualTo(4.2);
                    assertThat(metric.unit()).isEqualTo("C");
                });
    }
}
