package com.example.minibilling.service;

import com.example.minibilling.model.domain.CustomerResponse;
import com.example.minibilling.repository.ReadingRepository;
import com.example.minibilling.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;

    public CustomerService(UserRepository userRepository,
                           ReadingRepository readingRepository) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
    }

    public List<CustomerResponse> getCustomers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    int readingCount = readingRepository
                            .findByCustomerReference(user.reference()).size();
                    String status = resolveStatus(readingCount);
                    return new CustomerResponse(
                            user.reference(),
                            user.name(),
                            user.priceListNumber(),
                            status
                    );
                })
                .toList();
    }

    private String resolveStatus(int readingCount) {
        if (readingCount >= 2) return "VALID";
        if (readingCount == 1) return "WARNING";
        return "ERROR";
    }
}