package com.example.csvpointbypoint.service;

import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.entity.*;
import com.example.csvpointbypoint.mapper.PointByPointMapper;
import com.example.csvpointbypoint.repository.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import static org.mockito.Mockito.*;

class PointByPointPersistenceServiceTest {
    @Test
    void duplicateProcessedEventIsIgnored() {
        var rows=mock(PointByPointEventRepository.class);
        var imports=mock(PointByPointImportRepository.class);
        var processed=mock(ProcessedFileEventRepository.class);
        var mapper=mock(PointByPointMapper.class);
        when(processed.existsById("e1")).thenReturn(true);
        var service=new PointByPointPersistenceService(rows,imports,processed,mapper);
        var value=mock(PointByPointValue.class);
        when(value.getSourceEventId()).thenReturn("e1");
        service.handle(value);
        verifyNoInteractions(rows,imports,mapper);
    }
}
