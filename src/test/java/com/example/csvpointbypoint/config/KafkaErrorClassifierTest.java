package com.example.csvpointbypoint.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorClassifierTest {
    private final KafkaErrorClassifier classifier = new KafkaErrorClassifier();

    @Test
    void treatsInvalidProtocolOrDataAsNonRetryable() {
        assertThat(classifier.isRetryable(new IllegalArgumentException("unsupported event"))).isFalse();
        assertThat(classifier.isRetryable(new IllegalStateException("invalid import state"))).isFalse();
        assertThat(classifier.isRetryable(new DataIntegrityViolationException("constraint"))).isFalse();
    }

    @Test
    void treatsTransientDatabaseFailureAsRetryable() {
        assertThat(classifier.isRetryable(new TransientDataAccessResourceException("db unavailable"))).isTrue();
    }
}
