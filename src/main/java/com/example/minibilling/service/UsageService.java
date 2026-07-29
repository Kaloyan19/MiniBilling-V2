package com.example.minibilling.service;

import com.example.minibilling.model.domain.Reading;
import com.example.minibilling.model.domain.UsageResponse;
import com.example.minibilling.model.domain.User;
import com.example.minibilling.repository.ReadingRepository;
import com.example.minibilling.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UsageService {

    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;

    public UsageService(UserRepository userRepository,
                        ReadingRepository readingRepository) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
    }

    public List<UsageResponse> getUsageData() {
        List<UsageResponse> result = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            List<Reading> readings = readingRepository
                    .findByCustomerReference(user.reference())
                    .stream()
                    .sorted(Comparator.comparing(Reading::date))
                    .toList();

            double previousConsumption = 0;

            for (int i = 0; i < readings.size() - 1; i++) {
                Reading from = readings.get(i);
                Reading to = readings.get(i + 1);
                double consumption = to.meterReading() - from.meterReading();

                String status = i == 0
                        ? "VALID"
                        : resolveStatus(consumption, previousConsumption);

                previousConsumption = consumption;

                result.add(new UsageResponse(
                        user.reference(),
                        user.name(),
                        from.date(),
                        to.date(),
                        consumption,
                        status
                ));
            }
        }
        return result;
    }

    private String resolveStatus(double current, double previous) {
        if (previous == 0) return "VALID";
        double deviation = Math.abs(current - previous) / previous * 100;
        return deviation > 50 ? "WARNING" : "VALID";
    }
}