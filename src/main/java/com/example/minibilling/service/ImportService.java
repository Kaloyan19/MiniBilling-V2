package com.example.minibilling.service;

import com.example.minibilling.model.entity.FileImportEntity;
import com.example.minibilling.model.entity.ImportType;
import com.example.minibilling.repository.jpa.FileImportEntityRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ImportService {

    private final FileImportEntityRepository fileImportRepository;

    public ImportService(FileImportEntityRepository fileImportRepository) {
        this.fileImportRepository = fileImportRepository;
    }

    public void saveFileImport(String filename) {
        FileImportEntity entity = new FileImportEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setFilename(filename);
        entity.setUploadedAt(OffsetDateTime.now());
        entity.setType(resolveImportType(filename));
        fileImportRepository.save(entity);
    }

    public List<FileImportEntity> getRecentImports() {
        return fileImportRepository.findTop5ByOrderByUploadedAtDesc();
    }

    private ImportType resolveImportType(String filename) {
        if (filename.startsWith("users")) return ImportType.USERS;
        if (filename.startsWith("prices")) return ImportType.PRICES;
        return ImportType.READINGS;
    }
}