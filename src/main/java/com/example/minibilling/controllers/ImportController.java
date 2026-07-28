package com.example.minibilling.controllers;

import com.example.minibilling.importer.FileImporter;
import com.example.minibilling.model.entity.FileImportEntity;
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

    public ImportController(List<FileImporter> importers, ImportService importService){
        this.importers = importers;
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();

        FileImporter importer = importers.stream()
                .filter(i -> i.supports(filename))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Неподдържан файл: " + filename));

        importer.importFile(file);
        importService.saveFileImport(filename);
        return ResponseEntity.ok("Файлът е импортиран успешно: " + filename);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FileImportEntity>> getRecentImports() {
        return ResponseEntity.ok(importService.getRecentImports());
    }
}
