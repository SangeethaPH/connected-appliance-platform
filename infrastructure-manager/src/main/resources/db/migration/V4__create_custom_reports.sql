CREATE TABLE custom_reports (
    id UUID PRIMARY KEY,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    total_samples BIGINT NOT NULL,
    report_content TEXT NOT NULL,
    CONSTRAINT custom_report_valid_period CHECK (period_end > period_start)
);

CREATE INDEX idx_custom_reports_generated_at ON custom_reports(generated_at DESC);
