package com.example.csvpointbypoint;

import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.entity.PointByPointEvent;
import com.example.csvpointbypoint.mapper.PointByPointMapper;
import com.example.csvpointbypoint.repository.PointByPointEventRepository;
import com.example.csvpointbypoint.repository.PointByPointEventUpsertRepository;
import com.example.csvpointbypoint.repository.PointByPointImportRepository;
import com.example.csvpointbypoint.repository.ProcessedFileEventRepository;
import com.example.csvpointbypoint.service.PointByPointPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.properties.schema.registry.url=mock://pbp-batch-it"
})
class PointByPointBatchPersistenceIT {

    private static final int BENCHMARK_ROWS = 1_000;

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("pbp_batch")
                    .withUsername("pbp")
                    .withPassword("pbp");

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired PointByPointPersistenceService service;
    @Autowired PointByPointEventRepository rows;
    @Autowired PointByPointImportRepository imports;
    @Autowired ProcessedFileEventRepository processed;
    @Autowired PointByPointEventUpsertRepository rowUpserts;
    @Autowired PointByPointMapper mapper;

    @BeforeEach
    void cleanDatabase() {
        processed.deleteAll();
        rows.deleteAll();
        imports.deleteAll();
    }

    @Test
    void kafkaBatchPersistsRowsAndCompletesImport() {
        service.handleBatch(List.of(
                control("e-batch", "START", 3),
                row("e-batch", 1L, 1),
                row("e-batch", 2L, 2),
                row("e-batch", 3L, 3),
                control("e-batch", "COMPLETED", 3)));

        assertEquals(3, rows.countByIdSourceEventId("e-batch"));
        assertEquals(3L, service.get("e-batch").getRowsPersisted());
        assertEquals("COMPLETED", service.get("e-batch").getStatus().name());
        assertTrue(processed.existsById("e-batch"));
    }

    @Test
    void failedCompletionRollsBackWholeKafkaBatch() {
        assertThrows(IllegalStateException.class, () -> service.handleBatch(List.of(
                control("e-rollback", "START", 2),
                row("e-rollback", 1L, 1),
                control("e-rollback", "COMPLETED", 2))));

        assertEquals(0, rows.countByIdSourceEventId("e-rollback"));
        assertTrue(imports.findById("e-rollback").isEmpty());
        assertTrue(processed.findById("e-rollback").isEmpty());
    }

    @Test
    void measuresSequentialVersusJdbcBatchThroughput() {
        List<PointByPointEvent> events = new ArrayList<>(BENCHMARK_ROWS);
        for (int i = 1; i <= BENCHMARK_ROWS; i++) {
            events.add(mapper.toEntity(row("e-benchmark", i, i)));
        }

        long sequentialStart = System.nanoTime();
        events.forEach(rowUpserts::upsert);
        long sequentialNanos = System.nanoTime() - sequentialStart;
        assertEquals(BENCHMARK_ROWS, rows.count());

        rows.deleteAll();

        long batchStart = System.nanoTime();
        rowUpserts.upsertBatch(events);
        long batchNanos = System.nanoTime() - batchStart;
        assertEquals(BENCHMARK_ROWS, rows.count());

        double sequentialRowsPerSecond =
                BENCHMARK_ROWS / (sequentialNanos / 1_000_000_000.0);
        double batchRowsPerSecond =
                BENCHMARK_ROWS / (batchNanos / 1_000_000_000.0);

        System.out.printf(
                "KAN-128 throughput rows=%d sequential=%.0f rows/s batch=%.0f rows/s speedup=%.2fx%n",
                BENCHMARK_ROWS,
                sequentialRowsPerSecond,
                batchRowsPerSecond,
                batchRowsPerSecond / sequentialRowsPerSecond);

        assertTrue(batchNanos < sequentialNanos,
                "JDBC batch should reduce PostgreSQL round-trips for a representative import");
    }

    private PointByPointValue control(String eventId, String type, long expectedRows) {
        return PointByPointValue.newBuilder()
                .setSourceEventId(eventId)
                .setEventType(type)
                .setFilePath("/data/" + eventId + ".csv")
                .setExpectedRows(expectedRows)
                .setRowNumber(0L)
                .setMatchId("m1")
                .build();
    }

    private PointByPointValue row(String eventId, long rowNumber, int sequence) {
        return PointByPointValue.newBuilder()
                .setSourceEventId(eventId)
                .setEventType("ROW")
                .setFilePath("/data/" + eventId + ".csv")
                .setExpectedRows(BENCHMARK_ROWS)
                .setRowNumber(rowNumber)
                .setMatchId("m1")
                .setRecordType("point_event")
                .setQuarter("Q1")
                .setSequence(sequence)
                .setHomeScore(sequence)
                .setAwayScore(0)
                .setHomePointsAdded(1)
                .setAwayPointsAdded(0)
                .setHomeIsWinning(true)
                .setAwayIsWinning(false)
                .build();
    }
}
