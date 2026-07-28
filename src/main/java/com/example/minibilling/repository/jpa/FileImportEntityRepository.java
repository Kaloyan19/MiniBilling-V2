package com.example.minibilling.repository.jpa;

import com.example.minibilling.model.entity.FileImportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileImportEntityRepository extends JpaRepository<FileImportEntity, String> {
    List<FileImportEntity> findTop5ByOrderByUploadedAtDesc();
}