CREATE TABLE IF NOT EXISTS point_by_point_score (
    match_id VARCHAR(255) NOT NULL,
    score VARCHAR(255) NOT NULL,
    PRIMARY KEY (match_id, score)
);

CREATE TABLE IF NOT EXISTS processed_file_event (
    event_id VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
