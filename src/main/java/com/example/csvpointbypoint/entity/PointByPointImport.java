package com.example.csvpointbypoint.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name="point_by_point_import")
@Data
public class PointByPointImport {
    @Id @Column(name="event_id",length=255) private String eventId;
    @Column(name="file_path",nullable=false,columnDefinition="text") private String filePath;
    @Enumerated(EnumType.STRING) @Column(name="status",nullable=false,length=16) private ImportStatus status;
    @Column(name="expected_rows") private Long expectedRows;
    @Column(name="rows_read",nullable=false) private long rowsRead;
    @Column(name="rows_persisted",nullable=false) private long rowsPersisted;
    @Column(name="started_at",nullable=false) private Instant startedAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Column(name="completed_at") private Instant completedAt;
    @Column(name="error",columnDefinition="text") private String error;
}
