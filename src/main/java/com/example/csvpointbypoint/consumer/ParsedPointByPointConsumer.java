package com.example.csvpointbypoint.consumer;

import com.example.csvpointbypoint.avro.PointByPointKey;
import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.service.PointByPointPersistenceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParsedPointByPointConsumer {
    private final PointByPointPersistenceService service;

    public ParsedPointByPointConsumer(PointByPointPersistenceService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.parsed-point-by-point}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(List<ConsumerRecord<PointByPointKey, PointByPointValue>> records) {
        service.handleBatch(records.stream().map(ConsumerRecord::value).toList());
    }
}
