CREATE TABLE vendors (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    auth_type VARCHAR(30) NOT NULL,
    auth_username VARCHAR(200) NOT NULL,
    auth_password_encrypted VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE appliances (
    id UUID PRIMARY KEY,
    vendor_id UUID NOT NULL REFERENCES vendors(id),
    external_id VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    onboarded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_appliance_vendor_external UNIQUE (vendor_id, external_id)
);

CREATE INDEX idx_appliances_vendor_id ON appliances(vendor_id);
