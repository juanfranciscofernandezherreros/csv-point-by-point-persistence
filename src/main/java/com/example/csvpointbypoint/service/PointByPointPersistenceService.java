package com.example.csvpointbypoint.service;

import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.entity.ImportStatus;
import com.example.csvpointbypoint.entity.PointByPointEvent;
import com.example.csvpointbypoint.entity.PointByPointImport;
import com.example.csvpointbypoint.entity.ProcessedFileEvent;
import com.example.csvpointbypoint.mapper.PointByPointMapper;
import com.example.csvpointbypoint.repository.PointByPointEventRepository;
import com.example.csvpointbypoint.repository.PointByPointEventUpsertRepository;
import com.example.csvpointbypoint.repository.PointByPointImportRepository;
import com.example.csvpointbypoint.repository.ProcessedFileEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PointByPointPersistenceService {
    private final PointByPointEventRepository rows;
    private final PointByPointEventUpsertRepository rowUpserts;
    private final PointByPointImportRepository imports;
    private final ProcessedFileEventRepository processed;
    private final PointByPointMapper mapper;

    public PointByPointPersistenceService(PointByPointEventRepository rows,
                                          PointByPointEventUpsertRepository rowUpserts,
                                          PointByPointImportRepository imports,
                                          ProcessedFileEventRepository processed,
                                          PointByPointMapper mapper) {
        this.rows = rows;
        this.rowUpserts = rowUpserts;
        this.imports = imports;
        this.processed = processed;
        this.mapper = mapper;
    }

    @Transactional
    public void handle(PointByPointValue value) {
        handleBatch(List.of(value));
    }

    @Transactional
    public void handleBatch(List<PointByPointValue> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        Map<String, Boolean> processedCache = new HashMap<>();
        List<PointByPointValue> pendingRows = new ArrayList<>();
        String pendingEventId = null;

        for (PointByPointValue value : values) {
            String eventId = value.getSourceEventId();

            if ("ROW".equals(value.getEventType())) {
                if (isProcessed(eventId, processedCache)) {
                    continue;
                }

                if (pendingEventId != null && !pendingEventId.equals(eventId)) {
                    persistRows(pendingRows);
                    pendingRows.clear();
                }
                pendingEventId = eventId;
                pendingRows.add(value);
                continue;
            }

            if (!pendingRows.isEmpty()) {
                persistRows(pendingRows);
                pendingRows.clear();
                pendingEventId = null;
            }

            if (isProcessed(eventId, processedCache)) {
                continue;
            }

            switch (value.getEventType()) {
                case "START" -> start(value);
                case "COMPLETED" -> {
                    complete(value);
                    processedCache.put(eventId, true);
                }
                case "FAILED" -> fail(value);
                default -> throw new IllegalArgumentException(
                        "Unsupported eventType: " + value.getEventType());
            }
        }

        if (!pendingRows.isEmpty()) {
            persistRows(pendingRows);
        }
    }

    private boolean isProcessed(String eventId, Map<String, Boolean> processedCache) {
        return processedCache.computeIfAbsent(eventId, processed::existsById);
    }

    private void start(PointByPointValue value) {
        String eventId = value.getSourceEventId();
        Instant now = Instant.now();
        PointByPointImport job = imports.findById(eventId).orElse(null);

        if (job == null) {
            job = new PointByPointImport();
            job.setEventId(eventId);
            job.setRowsRead(0);
            job.setRowsPersisted(0);
            job.setStartedAt(now);
        }

        job.setFilePath(value.getFilePath());
        job.setStatus(ImportStatus.IMPORTING);
        job.setExpectedRows(value.getExpectedRows());
        job.setUpdatedAt(now);
        job.setCompletedAt(null);
        job.setError(null);
        imports.save(job);
    }

    private void persistRows(List<PointByPointValue> values) {
        if (values.isEmpty()) {
            return;
        }

        String eventId = values.get(0).getSourceEventId();
        PointByPointImport job = require(eventId);
        if (job.getStatus() != ImportStatus.IMPORTING) {
            throw new IllegalStateException("Expected IMPORTING for event " + eventId);
        }

        long maxRowNumber = job.getRowsRead();
        List<PointByPointEvent> entities = new ArrayList<>(values.size());

        for (PointByPointValue value : values) {
            if (!eventId.equals(value.getSourceEventId())) {
                throw new IllegalArgumentException("ROW batch contains more than one sourceEventId");
            }
            maxRowNumber = Math.max(maxRowNumber, value.getRowNumber());
            entities.add(mapper.toEntity(value));
        }

        rowUpserts.upsertBatch(entities);

        long rowsPersisted = rows.countByIdSourceEventId(eventId);
        job.setRowsRead(maxRowNumber);
        job.setRowsPersisted(rowsPersisted);
        job.setUpdatedAt(Instant.now());
        imports.save(job);
    }

    private void complete(PointByPointValue value) {
        PointByPointImport job = require(value.getSourceEventId());
        long expected = value.getExpectedRows();

        if (job.getRowsRead() != expected) {
            throw new IllegalStateException(
                    "Received " + job.getRowsRead() + " rows; expected " + expected);
        }

        long stored = rows.countByIdSourceEventId(value.getSourceEventId());
        if (stored != expected) {
            throw new IllegalStateException(
                    "Database has " + stored + " rows for import "
                            + value.getSourceEventId() + "; expected " + expected);
        }

        Instant now = Instant.now();
        processed.save(new ProcessedFileEvent(value.getSourceEventId(), now));
        job.setStatus(ImportStatus.COMPLETED);
        job.setRowsPersisted(stored);
        job.setCompletedAt(now);
        job.setUpdatedAt(now);
        imports.save(job);
    }

    private void fail(PointByPointValue value) {
        PointByPointImport job = imports.findById(value.getSourceEventId())
                .orElseGet(PointByPointImport::new);
        Instant now = Instant.now();
        job.setEventId(value.getSourceEventId());
        job.setFilePath(value.getFilePath());
        job.setStatus(ImportStatus.FAILED);
        job.setExpectedRows(value.getExpectedRows());
        if (job.getStartedAt() == null) {
            job.setStartedAt(now);
        }
        job.setUpdatedAt(now);
        job.setCompletedAt(null);
        job.setError(value.getError());
        imports.save(job);
    }

    @Transactional(readOnly = true)
    public PointByPointImport get(String eventId) {
        return imports.findById(eventId).orElseThrow(() ->
                new NoSuchElementException("No import for event " + eventId));
    }

    private PointByPointImport require(String eventId) {
        return imports.findById(eventId).orElseThrow(() ->
                new NoSuchElementException("No import for event " + eventId));
    }
}
