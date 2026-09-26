ALTER TABLE point_by_point_event
    ADD COLUMN source_event_id VARCHAR(255);

UPDATE point_by_point_event
SET source_event_id = 'legacy:' || match_id
WHERE source_event_id IS NULL;

ALTER TABLE point_by_point_event
    ALTER COLUMN source_event_id SET NOT NULL;

ALTER TABLE point_by_point_event
    DROP CONSTRAINT point_by_point_event_pkey;

ALTER TABLE point_by_point_event
    ADD PRIMARY KEY (source_event_id, match_id, quarter, sequence);

CREATE INDEX idx_point_by_point_event_match_id
    ON point_by_point_event(match_id);

CREATE INDEX idx_point_by_point_event_source_event_id
    ON point_by_point_event(source_event_id);
