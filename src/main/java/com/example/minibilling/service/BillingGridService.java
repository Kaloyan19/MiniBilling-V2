package com.example.minibilling.service;

import com.example.minibilling.model.domain.PricePeriod;
import com.example.minibilling.model.domain.Reading;
import com.example.minibilling.repository.PriceRepository;
import com.example.minibilling.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillingGridService {
    private final ReadingRepository readingRepository;
    private final PriceRepository priceRepository;

    public BillingGridService(ReadingRepository readingRepository, PriceRepository priceRepository) {
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
    }

    public List<Reading> getAllReadings() {
        return readingRepository.findAll();
    }

    public List<PricePeriod> getAllPricePeriods() {
        return priceRepository.findAll();
    }

}
