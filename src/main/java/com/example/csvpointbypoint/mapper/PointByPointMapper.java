package com.example.csvpointbypoint.mapper;

import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.entity.PointByPointEvent;
import com.example.csvpointbypoint.entity.PointByPointEventId;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PointByPointMapper {
    public PointByPointEvent toEntity(PointByPointValue v) {
        PointByPointEvent entity = new PointByPointEvent();
        entity.setId(new PointByPointEventId(
                Objects.requireNonNull(v.getSourceEventId(),"sourceEventId"),
                Objects.requireNonNull(v.getMatchId(),"matchId"),
                Objects.requireNonNull(v.getQuarter(),"quarter"),
                Objects.requireNonNull(v.getSequence(),"sequence")));
        entity.setRecordType(Objects.requireNonNull(v.getRecordType(),"recordType"));
        entity.setHomeScore(Objects.requireNonNull(v.getHomeScore(),"homeScore"));
        entity.setAwayScore(Objects.requireNonNull(v.getAwayScore(),"awayScore"));
        entity.setHomePointsAdded(Objects.requireNonNull(v.getHomePointsAdded(),"homePointsAdded"));
        entity.setAwayPointsAdded(Objects.requireNonNull(v.getAwayPointsAdded(),"awayPointsAdded"));
        entity.setLeaderSide(v.getLeaderSide());
        entity.setAdvantage(v.getAdvantage());
        entity.setAdvantageDirection(v.getAdvantageDirection());
        entity.setHomeIsWinning(Objects.requireNonNull(v.getHomeIsWinning(),"homeIsWinning"));
        entity.setAwayIsWinning(Objects.requireNonNull(v.getAwayIsWinning(),"awayIsWinning"));
        return entity;
    }
}
