package com.example.csvpointbypoint.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="point_by_point_event")
@Data
public class PointByPointEvent {
    @EmbeddedId private PointByPointEventId id;
    @Column(name="record_type",nullable=false,length=64) private String recordType;
    @Column(name="home_score",nullable=false) private int homeScore;
    @Column(name="away_score",nullable=false) private int awayScore;
    @Column(name="home_points_added",nullable=false) private int homePointsAdded;
    @Column(name="away_points_added",nullable=false) private int awayPointsAdded;
    @Column(name="leader_side",length=16) private String leaderSide;
    @Column(name="advantage",length=32) private String advantage;
    @Column(name="advantage_direction",length=32) private String advantageDirection;
    @Column(name="home_is_winning",nullable=false) private boolean homeIsWinning;
    @Column(name="away_is_winning",nullable=false) private boolean awayIsWinning;
}
