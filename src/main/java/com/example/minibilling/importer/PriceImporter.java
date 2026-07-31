package com.example.minibilling.importer;

import com.example.minibilling.exception.ImportException;
import com.example.minibilling.model.domain.ProductType;
import com.example.minibilling.model.entity.PriceEntity;
import com.example.minibilling.repository.jpa.PriceEntityRepository;
import com.example.minibilling.validator.ImportValidator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class PriceImporter implements FileImporter {

    private final PriceEntityRepository priceEntityRepository;
    private final ImportValidator importValidator;

    public PriceImporter(PriceEntityRepository priceEntityRepository, ImportValidator importValidator) {
        this.priceEntityRepository = priceEntityRepository;
        this.importValidator = importValidator;
    }

    @Override
    public boolean supports(String filename) throws ImportException{
        return filename != null && filename.matches("prices-\\d+\\.csv");
    }

    @Transactional
    @Override
    public void importFile(MultipartFile file) throws ImportException {
        if (file.isEmpty()) {
            throw new ImportException("Файлът е празен!");
        }
        try {
            String filename = validateFilename(file);
            int priceListNumber = extractPriceListNumber(filename);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processLine(line, filename, priceListNumber);
                }
            }
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException("Грешка при импорт на цени: " + e.getMessage());
        }
    }

    private String validateFilename(MultipartFile file) throws ImportException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new ImportException("Името на файла не може да е null");
        }
        return filename;
    }

    private void processLine(String line, String filename, int priceListNumber) throws ImportException {
        String[] parts = line.trim().split("\\s*,\\s*");
        if (parts.length != 4) {
            throw new ImportException("Невалиден ред в " + filename + ": " + line);
        }
        checkForDuplicate(parts, priceListNumber);
        PriceEntity entity = buildEntity(parts, priceListNumber);
        importValidator.validatePrice(entity.getStartDate(), entity.getEndDate(), entity.getPrice());
        priceEntityRepository.save(entity);
    }

    private void checkForDuplicate(String[] parts, int priceListNumber) throws ImportException {
        if (priceEntityRepository.existsByProductAndStartDateAndPriceList(
                ProductType.valueOf(parts[0].toUpperCase()),
                LocalDate.parse(parts[1]),
                priceListNumber)) {
            throw new ImportException("Цена за " + parts[0] + " от " + parts[1] + " вече съществува!");
        }
    }

    private PriceEntity buildEntity(String[] parts, int priceListNumber) {
        PriceEntity entity = new PriceEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setProduct(ProductType.valueOf(parts[0].toUpperCase()));
        entity.setStartDate(LocalDate.parse(parts[1]));
        entity.setEndDate(LocalDate.parse(parts[2]));
        entity.setPrice(Double.parseDouble(parts[3]));
        entity.setPriceList(priceListNumber);
        return entity;
    }

    private int extractPriceListNumber(String filename) {
        return Integer.parseInt(filename.replace("prices-", "").replace(".csv", ""));
    }
}