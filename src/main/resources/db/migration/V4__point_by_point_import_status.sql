CREATE TABLE point_by_point_import (
    event_id VARCHAR(255) PRIMARY KEY,
    file_path TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    expected_rows BIGINT,
    rows_read BIGINT NOT NULL,
    rows_persisted BIGINT NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    error TEXT
);
