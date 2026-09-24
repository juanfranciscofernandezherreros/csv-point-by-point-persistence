CREATE TABLE point_by_point_event (
    match_id VARCHAR(255) NOT NULL,
    quarter VARCHAR(64) NOT NULL,
    sequence INTEGER NOT NULL,
    record_type VARCHAR(64) NOT NULL,
    home_score INTEGER NOT NULL,
    away_score INTEGER NOT NULL,
    home_points_added INTEGER NOT NULL,
    away_points_added INTEGER NOT NULL,
    leader_side VARCHAR(16),
    advantage VARCHAR(32),
    advantage_direction VARCHAR(32),
    home_is_winning BOOLEAN NOT NULL,
    away_is_winning BOOLEAN NOT NULL,
    PRIMARY KEY (match_id, quarter, sequence)
);
