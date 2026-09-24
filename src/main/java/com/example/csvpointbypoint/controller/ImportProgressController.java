package com.example.csvpointbypoint.controller;

import com.example.csvpointbypoint.service.PointByPointPersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.NoSuchElementException;

@RestController
public class ImportProgressController {
    private final PointByPointPersistenceService service;
    public ImportProgressController(PointByPointPersistenceService service) { this.service=service; }

    @GetMapping("/imports/{eventId}")
    public ImportProgressResponse get(@PathVariable String eventId) {
        try { return ImportProgressResponse.from(service.get(eventId)); }
        catch (NoSuchElementException e) { throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Import not found",e); }
    }
}
