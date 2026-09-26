package com.example.csvpointbypoint.repository;

import com.example.csvpointbypoint.entity.PointByPointEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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
        jdbcTemplate.update(UPSERT_SQL, ps -> bind(ps, event));
    }

    public void upsertBatch(List<PointByPointEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                UPSERT_SQL,
                events,
                events.size(),
                (ps, event) -> bind(ps, event));
    }

    private void bind(PreparedStatement ps, PointByPointEvent event) throws SQLException {
        ps.setString(1, event.getId().getSourceEventId());
        ps.setString(2, event.getId().getMatchId());
        ps.setString(3, event.getId().getQuarter());
        ps.setInt(4, event.getId().getSequence());
        ps.setString(5, event.getRecordType());
        ps.setInt(6, event.getHomeScore());
        ps.setInt(7, event.getAwayScore());
        ps.setInt(8, event.getHomePointsAdded());
        ps.setInt(9, event.getAwayPointsAdded());
        ps.setString(10, event.getLeaderSide());
        ps.setString(11, event.getAdvantage());
        ps.setString(12, event.getAdvantageDirection());
        ps.setBoolean(13, event.isHomeIsWinning());
        ps.setBoolean(14, event.isAwayIsWinning());
    }
}
