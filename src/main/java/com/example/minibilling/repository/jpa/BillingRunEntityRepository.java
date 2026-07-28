package com.example.minibilling.repository.jpa;

import com.example.minibilling.model.entity.BillingRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRunEntityRepository extends JpaRepository<BillingRunEntity, String> {
    BillingRunEntity findTopByOrderByEndDateDesc();
}