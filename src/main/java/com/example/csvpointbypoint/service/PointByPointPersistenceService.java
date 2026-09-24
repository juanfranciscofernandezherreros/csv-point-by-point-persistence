package com.example.csvpointbypoint.service;

import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.entity.*;
import com.example.csvpointbypoint.mapper.PointByPointMapper;
import com.example.csvpointbypoint.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class PointByPointPersistenceService {
    private final PointByPointEventRepository rows;
    private final PointByPointImportRepository imports;
    private final ProcessedFileEventRepository processed;
    private final PointByPointMapper mapper;

    public PointByPointPersistenceService(PointByPointEventRepository rows,
                                          PointByPointImportRepository imports,
                                          ProcessedFileEventRepository processed,
                                          PointByPointMapper mapper) {
        this.rows=rows; this.imports=imports; this.processed=processed; this.mapper=mapper;
    }

    @Transactional
    public void handle(PointByPointValue value) {
        String eventId = value.getSourceEventId();
        if (processed.existsById(eventId)) return;

        switch (value.getEventType()) {
            case "START" -> start(value);
            case "ROW" -> persistRow(value);
            case "COMPLETED" -> complete(value);
            case "FAILED" -> fail(value);
            default -> throw new IllegalArgumentException("Unsupported eventType: " + value.getEventType());
        }
    }

    private void start(PointByPointValue value) {
        PointByPointImport job = imports.findById(value.getSourceEventId()).orElseGet(PointByPointImport::new);
        Instant now = Instant.now();
        job.setEventId(value.getSourceEventId());
        job.setFilePath(value.getFilePath());
        job.setStatus(ImportStatus.IMPORTING);
        job.setExpectedRows(value.getExpectedRows());
        job.setRowsRead(0);
        job.setRowsPersisted(0);
        if (job.getStartedAt() == null) job.setStartedAt(now);
        job.setUpdatedAt(now);
        job.setCompletedAt(null);
        job.setError(null);
        imports.save(job);
    }

    private void persistRow(PointByPointValue value) {
        PointByPointImport job = require(value.getSourceEventId());
        if (job.getStatus() != ImportStatus.IMPORTING) {
            throw new IllegalStateException("Expected IMPORTING for event " + value.getSourceEventId());
        }
        rows.save(mapper.toEntity(value));
        long progress = Math.max(job.getRowsRead(), value.getRowNumber());
        job.setRowsRead(progress);
        job.setRowsPersisted(progress);
        job.setUpdatedAt(Instant.now());
        imports.save(job);
    }

    private void complete(PointByPointValue value) {
        PointByPointImport job = require(value.getSourceEventId());
        job.setStatus(ImportStatus.VERIFYING);
        long expected = value.getExpectedRows();
        if (job.getRowsRead() != expected) {
            throw new IllegalStateException("Received " + job.getRowsRead() + " rows; expected " + expected);
        }
        if (expected > 0) {
            if (value.getMatchId() == null) throw new IllegalStateException("COMPLETED event requires matchId");
            long stored = rows.countByIdMatchId(value.getMatchId());
            if (stored != expected) {
                throw new IllegalStateException("Database has " + stored + " rows for match "
                        + value.getMatchId() + "; expected " + expected);
            }
        }
        Instant now = Instant.now();
        processed.save(new ProcessedFileEvent(value.getSourceEventId(), now));
        job.setStatus(ImportStatus.COMPLETED);
        job.setRowsPersisted(expected);
        job.setCompletedAt(now);
        job.setUpdatedAt(now);
        imports.save(job);
    }

    private void fail(PointByPointValue value) {
        PointByPointImport job = imports.findById(value.getSourceEventId()).orElseGet(PointByPointImport::new);
        Instant now = Instant.now();
        job.setEventId(value.getSourceEventId());
        job.setFilePath(value.getFilePath());
        job.setStatus(ImportStatus.FAILED);
        job.setExpectedRows(value.getExpectedRows());
        if (job.getStartedAt() == null) job.setStartedAt(now);
        job.setUpdatedAt(now);
        job.setCompletedAt(null);
        job.setError(value.getError());
        imports.save(job);
    }

    @Transactional(readOnly=true)
    public PointByPointImport get(String eventId) {
        return imports.findById(eventId).orElseThrow(() ->
                new NoSuchElementException("No import for event " + eventId));
    }

    private PointByPointImport require(String eventId) {
        return imports.findById(eventId).orElseThrow(() ->
                new NoSuchElementException("No import for event " + eventId));
    }
}
