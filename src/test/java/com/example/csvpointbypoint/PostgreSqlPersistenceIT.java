package com.example.csvpointbypoint;

import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.repository.PointByPointEventRepository;
import com.example.csvpointbypoint.repository.ProcessedFileEventRepository;
import com.example.csvpointbypoint.service.PointByPointPersistenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(properties={"spring.kafka.listener.auto-startup=false","spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.properties.schema.registry.url=mock://pbp-it"})
class PostgreSqlPersistenceIT {
    @Container static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine").withDatabaseName("pbp").withUsername("pbp").withPassword("pbp");

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username",POSTGRES::getUsername);
        r.add("spring.datasource.password",POSTGRES::getPassword);
    }

    @Autowired PointByPointPersistenceService service;
    @Autowired PointByPointEventRepository rows;
    @Autowired ProcessedFileEventRepository processed;

    @Test
    void startRowAndCompletePersistIdempotently() {
        service.handle(control("START"));
        service.handle(row());
        service.handle(control("COMPLETED"));
        service.handle(row());
        assertEquals(1,rows.count());
        assertTrue(processed.existsById("e1"));
        assertEquals(1L,service.get("e1").getRowsPersisted());
        assertEquals("COMPLETED",service.get("e1").getStatus().name());
    }

    private PointByPointValue control(String type) {
        return PointByPointValue.newBuilder().setSourceEventId("e1").setEventType(type)
                .setFilePath("/data/pbp.csv").setExpectedRows(1L).setRowNumber(0L).setMatchId("m1").build();
    }

    private PointByPointValue row() {
        return PointByPointValue.newBuilder().setSourceEventId("e1").setEventType("ROW")
                .setFilePath("/data/pbp.csv").setExpectedRows(1L).setRowNumber(1L)
                .setMatchId("m1").setRecordType("point_event").setQuarter("Q1").setSequence(1)
                .setHomeScore(2).setAwayScore(0).setHomePointsAdded(2).setAwayPointsAdded(0)
                .setHomeIsWinning(true).setAwayIsWinning(false).build();
    }
}
