package com.example.minibilling.importer;

import com.example.minibilling.exception.ImportException;
import com.example.minibilling.model.domain.ProductType;
import com.example.minibilling.model.entity.ReadingEntity;
import com.example.minibilling.model.entity.UserEntity;
import com.example.minibilling.repository.jpa.ReadingEntityRepository;
import com.example.minibilling.repository.jpa.UserEntityRepository;
import com.example.minibilling.validator.ImportValidator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class ReadingImporter implements FileImporter{

    private final ReadingEntityRepository readingEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final ImportValidator importValidator;

    public ReadingImporter(ReadingEntityRepository readingEntityRepository, UserEntityRepository userEntityRepository, ImportValidator importValidator) {
        this.readingEntityRepository = readingEntityRepository;
        this.userEntityRepository = userEntityRepository;
        this.importValidator = importValidator;
    }

    @Override
    public boolean supports(String filename) {
        return filename.matches("readings.*\\.csv");
    }

    @Transactional
    @Override
    public void importFile(MultipartFile file) throws ImportException {
        if (file.isEmpty()) {
            throw new ImportException("Файлът е празен!");
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException("Грешка при импорт: " + e.getMessage());
        }
    }

    private void processLine(String line) throws ImportException {
        String[] parts = line.trim().split("\\s*,\\s*");
        if (parts.length != 4) {
            throw new ImportException("Невалиден ред в readings.csv: " + line);
        }
        checkForDuplicate(parts);
        ReadingEntity entity = buildEntity(parts);
        importValidator.validateReading(parts[0], entity.getLastReading());
        readingEntityRepository.save(entity);
    }

    private void checkForDuplicate(String[] parts) throws ImportException {
        importValidator.validateReadingUser(parts[0]);
        if (readingEntityRepository.existsByUserReferenceAndDateTime(
                parts[0], OffsetDateTime.parse(parts[2]))) {
            throw new ImportException("Показание за потребител " + parts[0] + " на " + parts[2] + " вече съществува!");
        }
    }

    private ReadingEntity buildEntity(String[] parts) throws ImportException {
        UserEntity user = userEntityRepository.findByReference(parts[0]);
        ReadingEntity entity = new ReadingEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setUser(user);
        entity.setProduct(ProductType.valueOf(parts[1].toUpperCase()));
        entity.setDateTime(OffsetDateTime.parse(parts[2]));
        entity.setLastReading(new BigDecimal(parts[3]));
        entity.setInvoiced(false);
        entity.setSelfReported(false);
        return entity;
    }
}
