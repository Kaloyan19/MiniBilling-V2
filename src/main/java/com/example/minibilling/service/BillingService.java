package com.example.minibilling.service;

import com.example.minibilling.exception.UserNotFoundException;
import com.example.minibilling.model.domain.*;
import com.example.minibilling.repository.InvoiceRepository;
import com.example.minibilling.repository.PriceRepository;
import com.example.minibilling.repository.ReadingRepository;
import com.example.minibilling.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BillingService {

    private final DistributionService distributionService;
    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final PriceRepository priceRepository;
    private final AtomicInteger invoiceCounter = new AtomicInteger(10000);
    private final InvoiceRepository invoiceRepository;

    public BillingService(UserRepository userRepository, ReadingRepository readingRepository, PriceRepository priceRepository,
                          InvoiceRepository invoiceRepository, DistributionService distributionService){
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
        this.invoiceRepository = invoiceRepository;
        this.distributionService = distributionService;
    }

    public Optional<Invoice> generateAndSaveInvoice(String reference, LocalDate from, LocalDate to) {
        Optional<Invoice> invoice = generateInvoice(reference, from, to);
        if (invoice.isEmpty()) return Optional.empty();
        invoiceRepository.save(invoice.get(), reference, from + "_" + to);
        return invoice;
    }

    private Optional<Invoice> generateInvoice(String reference, LocalDate from, LocalDate to) {
        if (!from.isBefore(to)) {
            throw new DateTimeException("Началната дата трябва да е преди крайната!");
        }

        User user = userRepository.findByReference(reference);
        if (user == null) throw new UserNotFoundException(reference);

        List<Reading> readings = findReadings(user, from, to);
        if (readings.size() < 2) return Optional.empty();

        List<PricePeriod> prices = findPrices(user);
        List<InvoiceLine> lines = createInvoiceLines(readings, prices, user);
        return Optional.of(buildInvoice(user, lines));
    }

    private List<Reading> findReadings(User user, LocalDate from, LocalDate to) {
        List<Reading> readings = readingRepository.findByCustomerReference(user.reference())
                .stream()
                .filter(r -> !r.date().toLocalDate().isBefore(from))
                .filter(r -> !r.date().toLocalDate().isAfter(to))
                .sorted(Comparator.comparing(Reading::date))
                .toList();
        System.out.println("Readings for " + user.reference() + ": " + readings.size());
        return readings;
    }

    private List<PricePeriod> findPrices(User user) {
        return priceRepository.findByPriceList(user.priceListNumber());
    }

    private List<InvoiceLine> createInvoiceLines(List<Reading> readings,
                                                 List<PricePeriod> prices,
                                                 User user) {
        List<InvoiceLine> lines = new ArrayList<>();
        int index = 1;

        for (int i = 0; i < readings.size() - 1; i++) {
            Reading from = readings.get(i);
            Reading to = readings.get(i + 1);
            double totalQuantity = round2(to.meterReading() - from.meterReading());

            List<DistributionLine> distributed = distributionService.distribute(
                    from.date(), to.date(), totalQuantity, prices);

            for (DistributionLine dl : distributed) {
                lines.add(new InvoiceLine(index++, dl.quantity(), dl.start(), dl.end(),
                        from.product().name(), dl.price(),
                        user.priceListNumber(), round2(dl.quantity() * dl.price())));
            }
        }
        return lines;
    }

    // TODO: използва се за филтриране по продукт при допълнение 3
    private List<PricePeriod> findApplicablePricePeriods(
            List<PricePeriod> prices, Reading from, Reading to){
        LocalDate readingStart = from.date().toLocalDate();
        LocalDate readingEnd = to.date().toLocalDate();

        List<PricePeriod> applicable = prices.stream()
                .filter(p -> p.product() == from.product())
                .filter(p -> !p.startDate().isAfter(readingEnd))
                .filter(p -> !p.endDate().isBefore(readingStart))
                .toList();

        if (applicable.isEmpty()){
            throw new RuntimeException("Няма цена за периода: " + readingStart + " - " + readingEnd);
        }

        return applicable;
    }

    private Invoice buildInvoice(User user, List<InvoiceLine> lines) {
        double totalAmount = round2(lines.stream().
                mapToDouble(InvoiceLine::amount)
                .sum());
        return new Invoice(
                OffsetDateTime.now(),
                String.valueOf(invoiceCounter.getAndIncrement()),
                user.name(),
                user.reference(),
                totalAmount,
                lines
        );
    }

    private double round2(double value){
        return Math.ceil(value * 100) / 100.00;
    }

    private double round3(double value){
        return Math.ceil(value * 1000) / 100.00;
    }
}
