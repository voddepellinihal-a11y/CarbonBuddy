CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INTEGER,
    municipality VARCHAR(100),
    default_transit_mode VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS activities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    transit_mode VARCHAR(50),
    distance_km DOUBLE PRECISION,
    duration_minutes DOUBLE PRECISION,
    route_polyline TEXT,
    activity_start TIMESTAMP NOT NULL,
    activity_end TIMESTAMP,
    is_manual BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activities_user_id_start ON activities(user_id, activity_start);

CREATE TABLE IF NOT EXISTS utility_bills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_kwh DOUBLE PRECISION,
    utility_type VARCHAR(50),
    billing_start DATE,
    billing_end DATE,
    allocation_count INTEGER DEFAULT 1,
    status VARCHAR(20) DEFAULT 'PENDING',
    raw_ocr_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS carbon_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(50),
    carbon_kg DOUBLE PRECISION,
    distance_km DOUBLE PRECISION DEFAULT 0,
    source_type VARCHAR(50),
    source_id BIGINT,
    record_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_carbon_records_user_id_date ON carbon_records(user_id, record_date);
CREATE INDEX idx_carbon_records_category ON carbon_records(category);

CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT,
    description TEXT,
    estimated_savings_kg DOUBLE PRECISION,
    estimated_savings_percent DOUBLE PRECISION,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rewards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credits_earned INTEGER DEFAULT 0,
    credits_spent INTEGER DEFAULT 0,
    source VARCHAR(50),
    source_id BIGINT,
    transaction_type VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rewards_user_id ON rewards(user_id);
