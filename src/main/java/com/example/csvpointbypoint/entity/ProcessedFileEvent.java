package com.example.csvpointbypoint.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="processed_file_event")
public class ProcessedFileEvent {
    @Id @Column(name="event_id",nullable=false,updatable=false) private String eventId;
    @Column(name="processed_at",nullable=false) private Instant processedAt;
    protected ProcessedFileEvent() {}
    public ProcessedFileEvent(String eventId, Instant processedAt) { this.eventId=eventId; this.processedAt=processedAt; }
}
