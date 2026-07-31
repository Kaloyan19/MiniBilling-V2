package com.example.minibilling.controllers;

import com.example.minibilling.exception.ImportException;
import com.example.minibilling.importer.FileImporter;
import com.example.minibilling.model.entity.FileImportEntity;
import com.example.minibilling.model.entity.Severity;
import com.example.minibilling.service.ErrorLogService;
import com.example.minibilling.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/import")
@CrossOrigin(origins = "http://localhost:3000")
public class ImportController {

    private final List<FileImporter> importers;
    private final ImportService importService;
    private final ErrorLogService errorLogService;

    public ImportController(List<FileImporter> importers,
                            ImportService importService,
                            ErrorLogService errorLogService) {
        this.importers = importers;
        this.importService = importService;
        this.errorLogService = errorLogService;
    }

    @PostMapping
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();

        FileImporter importer = importers.stream()
                .filter(i -> i.supports(filename))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Неподдържан файл: " + filename));

        try {
            importer.importFile(file);
            importService.saveFileImport(filename);
            return ResponseEntity.ok("Файлът е импортиран успешно: " + filename);
        } catch (ImportException e) {
            errorLogService.log(
                    "IMPORT_ERROR",
                    e.getMessage(),
                    null,
                    "ImportController",
                    Severity.ERROR
            );
            throw e;
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FileImportEntity>> getRecentImports() {
        return ResponseEntity.ok(importService.getRecentImports());
    }
}
