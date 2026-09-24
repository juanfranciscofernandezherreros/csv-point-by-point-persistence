package com.example.csvpointbypoint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointByPointEventId implements Serializable {
    @Column(name="match_id",nullable=false,length=255) private String matchId;
    @Column(name="quarter",nullable=false,length=64) private String quarter;
    @Column(name="sequence",nullable=false) private Integer sequence;
}
