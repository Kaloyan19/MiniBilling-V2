package com.example.minibilling.controllers;

import com.example.minibilling.model.entity.ErrorLogEntity;
import com.example.minibilling.service.ErrorLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit")
@CrossOrigin(origins = "http://localhost:5173")
public class ErrorLogController {

    private final ErrorLogService errorLogService;

    public ErrorLogController(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ErrorLogEntity>> getRecentErrors() {
        return ResponseEntity.ok(errorLogService.getRecentErrors());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ErrorLogEntity>> getAllLogs() {
        return ResponseEntity.ok(errorLogService.getAllLogs());
    }
}