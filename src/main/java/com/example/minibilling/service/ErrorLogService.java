package com.example.minibilling.service;

import com.example.minibilling.model.entity.ErrorLogEntity;
import com.example.minibilling.model.entity.Severity;
import com.example.minibilling.repository.jpa.ErrorLogEntityRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ErrorLogService {

    private final ErrorLogEntityRepository errorLogRepository;

    public ErrorLogService(ErrorLogEntityRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    public void log(String errorType, String description, String customerId,
                    String module, Severity severity) {
        ErrorLogEntity entity = new ErrorLogEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setOccurredAt(OffsetDateTime.now());
        entity.setErrorType(errorType);
        entity.setDescription(description);
        entity.setCustomerId(customerId);
        entity.setModule(module);
        entity.setSeverity(severity);
        entity.setStatus("OPEN");
        errorLogRepository.save(entity);
    }

    public List<ErrorLogEntity> getRecentErrors() {
        return errorLogRepository.findTop5ByOrderByOccurredAtDesc();
    }

    public List<ErrorLogEntity> getAllLogs() {
        return errorLogRepository.findAll();
    }
}