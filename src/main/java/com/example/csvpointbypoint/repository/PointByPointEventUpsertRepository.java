package com.example.csvpointbypoint.repository;

import com.example.csvpointbypoint.entity.PointByPointEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PointByPointEventUpsertRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO point_by_point_event (
                source_event_id, match_id, quarter, sequence, record_type,
                home_score, away_score, home_points_added, away_points_added,
                leader_side, advantage, advantage_direction,
                home_is_winning, away_is_winning
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source_event_id, match_id, quarter, sequence) DO UPDATE SET
                record_type = EXCLUDED.record_type,
                home_score = EXCLUDED.home_score,
                away_score = EXCLUDED.away_score,
                home_points_added = EXCLUDED.home_points_added,
                away_points_added = EXCLUDED.away_points_added,
                leader_side = EXCLUDED.leader_side,
                advantage = EXCLUDED.advantage,
                advantage_direction = EXCLUDED.advantage_direction,
                home_is_winning = EXCLUDED.home_is_winning,
                away_is_winning = EXCLUDED.away_is_winning
            """;

    private final JdbcTemplate jdbcTemplate;

    public PointByPointEventUpsertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(PointByPointEvent event) {
        jdbcTemplate.update(
                UPSERT_SQL,
                event.getId().getSourceEventId(),
                event.getId().getMatchId(),
                event.getId().getQuarter(),
                event.getId().getSequence(),
                event.getRecordType(),
                event.getHomeScore(),
                event.getAwayScore(),
                event.getHomePointsAdded(),
                event.getAwayPointsAdded(),
                event.getLeaderSide(),
                event.getAdvantage(),
                event.getAdvantageDirection(),
                event.isHomeIsWinning(),
                event.isAwayIsWinning()
        );
    }
}
