package com.example.minibilling.repository;

import com.example.minibilling.model.domain.Reading;
import com.example.minibilling.model.entity.ReadingEntity;
import com.example.minibilling.repository.jpa.ReadingEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReadingRepository {

    private final ReadingEntityRepository readingEntityRepository;

    public ReadingRepository(ReadingEntityRepository readingEntityRepository){
        this.readingEntityRepository = readingEntityRepository;
    }

    public List<Reading> findByCustomerReference(String reference) {
        return readingEntityRepository.findByUserReference(reference)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Reading> findAll() {
        return readingEntityRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public Reading toDomain(ReadingEntity entity){
        return new Reading(
                entity.getUser().getReference(),
                entity.getProduct(),
                entity.getDateTime(),
                entity.getLastReading().doubleValue()
        );
    }
}
