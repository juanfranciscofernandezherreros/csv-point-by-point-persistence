package com.example.csvpointbypoint.controller;

import com.example.csvpointbypoint.entity.ImportStatus;
import com.example.csvpointbypoint.entity.PointByPointImport;
import java.time.Instant;

public record ImportProgressResponse(
        String eventId, ImportStatus status, Long expectedRows, long rowsRead, long rowsPersisted,
        Integer percent, String error, Instant startedAt, Instant updatedAt, Instant completedAt) {
    public static ImportProgressResponse from(PointByPointImport job) {
        Integer percent = null;
        if (job.getStatus() == ImportStatus.COMPLETED) percent = 100;
        else if (job.getExpectedRows() != null) {
            percent = job.getExpectedRows() == 0 ? 0
                    : (int)Math.min(99, job.getRowsRead() * 100.0 / job.getExpectedRows());
        }
        return new ImportProgressResponse(job.getEventId(),job.getStatus(),job.getExpectedRows(),
                job.getRowsRead(),job.getRowsPersisted(),percent,job.getError(),job.getStartedAt(),
                job.getUpdatedAt(),job.getCompletedAt());
    }
}
