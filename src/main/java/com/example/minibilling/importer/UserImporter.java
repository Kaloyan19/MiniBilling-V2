package com.example.minibilling.importer;

import com.example.minibilling.exception.ImportException;
import com.example.minibilling.model.entity.UserEntity;
import com.example.minibilling.repository.jpa.UserEntityRepository;
import com.example.minibilling.validator.ImportValidator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class UserImporter implements FileImporter {

    private final UserEntityRepository userEntityRepository;
    private final ImportValidator importValidator;

    public UserImporter(UserEntityRepository userEntityRepository, ImportValidator importValidator) {
        this.userEntityRepository = userEntityRepository;
        this.importValidator = importValidator;
    }

    @Override
    public boolean supports(String filename){
        return filename.matches("users.*\\.csv");
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
        if (parts.length != 3) {
            throw new ImportException("Невалиден ред в users.csv: " + line);
        }
        checkForDuplicate(parts);
        UserEntity entity = buildEntity(parts);
        importValidator.validateUser(entity.getName(), entity.getReference(), entity.getPriceList());
        userEntityRepository.save(entity);
    }

    private void checkForDuplicate(String[] parts) throws ImportException {
        if (userEntityRepository.findByReference(parts[1]) != null) {
            throw new ImportException("Потребител с референтен номер " + parts[1] + " вече съществува!");
        }
        importValidator.validateUserPriceList(Integer.parseInt(parts[2]));
    }

    private UserEntity buildEntity(String[] parts) {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setName(parts[0]);
        entity.setReference(parts[1]);
        entity.setPriceList(Integer.parseInt(parts[2]));
        return entity;
    }
}
