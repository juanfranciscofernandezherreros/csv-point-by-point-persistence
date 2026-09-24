package com.example.csvpointbypoint.repository;
import com.example.csvpointbypoint.entity.ProcessedFileEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedFileEventRepository extends JpaRepository<ProcessedFileEvent,String> {}
