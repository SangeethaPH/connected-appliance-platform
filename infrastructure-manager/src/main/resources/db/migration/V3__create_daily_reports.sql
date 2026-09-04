CREATE TABLE daily_reports (
    id UUID PRIMARY KEY,
    report_date DATE NOT NULL UNIQUE,
    zone_id VARCHAR(80) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    total_samples BIGINT NOT NULL,
    report_content TEXT NOT NULL
);

CREATE INDEX idx_daily_reports_generated_at ON daily_reports(generated_at DESC);
