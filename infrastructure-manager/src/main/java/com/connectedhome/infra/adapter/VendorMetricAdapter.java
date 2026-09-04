package com.connectedhome.infra.adapter;

import java.util.List;

import com.connectedhome.infra.messaging.RawMetricEvent;

public interface VendorMetricAdapter {
    boolean supports(String vendorCode);

    List<NormalizedMetric> normalize(RawMetricEvent event);
}
