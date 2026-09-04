CREATE TABLE appliance_metrics (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    appliance_id UUID NOT NULL REFERENCES appliances(id),
    metric_name VARCHAR(120) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(40) NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_metric_event_name UNIQUE (event_id, metric_name)
);

CREATE INDEX idx_metrics_appliance_captured
    ON appliance_metrics(appliance_id, captured_at DESC);
