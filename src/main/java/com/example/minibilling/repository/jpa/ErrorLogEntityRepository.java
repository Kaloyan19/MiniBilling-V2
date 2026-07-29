package com.example.minibilling.repository.jpa;

import com.example.minibilling.model.entity.ErrorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ErrorLogEntityRepository extends JpaRepository<ErrorLogEntity, String> {
    List<ErrorLogEntity> findTop5ByOrderByOccurredAtDesc();
}