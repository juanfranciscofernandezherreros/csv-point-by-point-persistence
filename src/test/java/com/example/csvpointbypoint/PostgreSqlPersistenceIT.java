package com.example.csvpointbypoint;

import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.repository.PointByPointEventRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(properties={
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.properties.schema.registry.url=mock://pbp-it"
})
class PostgreSqlPersistenceIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("pbp")
                    .withUsername("pbp")
                    .withPassword("pbp");

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username",POSTGRES::getUsername);
        r.add("spring.datasource.password",POSTGRES::getPassword);
    }

    @Autowired PointByPointPersistenceService service;
    @Autowired PointByPointEventRepository rows;
    @Autowired PointByPointImportRepository imports;
    @Autowired ProcessedFileEventRepository processed;

    @BeforeEach
    void cleanDatabase() {
        processed.deleteAll();
        rows.deleteAll();
        imports.deleteAll();
    }

    @Test
    void redeliveredStartAndRowsResumePartialImportWithoutDuplicates() {
        service.handle(control("e1","START",2));
        service.handle(row("e1",1L,"Q1",1,2,0));

        assertEquals(1L, service.get("e1").getRowsPersisted());

        service.handle(control("e1","START",2));
        assertEquals(1L, service.get("e1").getRowsPersisted());
        assertEquals(1L, service.get("e1").getRowsRead());

        service.handle(row("e1",1L,"Q1",1,2,0));
        service.handle(row("e1",2L,"Q1",2,4,0));
        service.handle(control("e1","COMPLETED",2));

        assertEquals(2, rows.count());
        assertEquals(2, rows.countByIdSourceEventId("e1"));
        assertTrue(processed.existsById("e1"));
        assertEquals(2L, service.get("e1").getRowsPersisted());
        assertEquals("COMPLETED", service.get("e1").getStatus().name());

        service.handle(control("e1","COMPLETED",2));
        service.handle(row("e1",2L,"Q1",2,4,0));

        assertEquals(2, rows.count());
        assertEquals("COMPLETED", service.get("e1").getStatus().name());
    }

    @Test
    void twoImportsOfSameMatchAreCountedIndependently() {
        service.handle(control("e1","START",1));
        service.handle(row("e1",1L,"Q1",1,2,0));
        service.handle(control("e1","COMPLETED",1));

        service.handle(control("e2","START",1));

        assertThrows(IllegalStateException.class,
                () -> service.handle(control("e2","COMPLETED",1)));

        service.handle(row("e2",1L,"Q1",1,3,0));
        service.handle(control("e2","COMPLETED",1));

        assertEquals(2, rows.count());
        assertEquals(1, rows.countByIdSourceEventId("e1"));
        assertEquals(1, rows.countByIdSourceEventId("e2"));
        assertEquals("COMPLETED", service.get("e1").getStatus().name());
        assertEquals("COMPLETED", service.get("e2").getStatus().name());
    }

    @Test
    void currentImportCountDetectsSparseDeliveryEvenWhenRowNumberReachedExpected() {
        service.handle(control("e1","START",2));
        service.handle(row("e1",2L,"Q1",2,4,0));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> service.handle(control("e1","COMPLETED",2)));

        assertTrue(error.getMessage().contains("Database has 1 rows for import e1; expected 2"));
        assertEquals("IMPORTING", service.get("e1").getStatus().name());
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

    private PointByPointValue row(
            String eventId,
            long rowNumber,
            String quarter,
            int sequence,
            int homeScore,
            int awayScore) {
        return PointByPointValue.newBuilder()
                .setSourceEventId(eventId)
                .setEventType("ROW")
                .setFilePath("/data/" + eventId + ".csv")
                .setExpectedRows(2L)
                .setRowNumber(rowNumber)
                .setMatchId("m1")
                .setRecordType("point_event")
                .setQuarter(quarter)
                .setSequence(sequence)
                .setHomeScore(homeScore)
                .setAwayScore(awayScore)
                .setHomePointsAdded(2)
                .setAwayPointsAdded(0)
                .setHomeIsWinning(true)
                .setAwayIsWinning(false)
                .build();
    }
}
