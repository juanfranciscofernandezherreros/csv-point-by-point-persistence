package com.example.csvpointbypoint.repository;

import com.example.csvpointbypoint.entity.PointByPointEvent;
import com.example.csvpointbypoint.entity.PointByPointEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointByPointEventRepository extends JpaRepository<PointByPointEvent,PointByPointEventId> {
    long countByIdMatchId(String matchId);
}
