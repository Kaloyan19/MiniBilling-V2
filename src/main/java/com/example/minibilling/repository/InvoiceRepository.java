package com.example.minibilling.repository;

import com.example.minibilling.model.domain.Invoice;
import com.example.minibilling.model.domain.InvoiceLine;
import com.example.minibilling.model.domain.InvoiceSummary;
import com.example.minibilling.model.domain.ProductType;
import com.example.minibilling.model.entity.InvoiceEntity;
import com.example.minibilling.model.entity.LineEntity;
import com.example.minibilling.model.entity.UserEntity;
import com.example.minibilling.repository.jpa.InvoiceEntityRepository;
import com.example.minibilling.repository.jpa.UserEntityRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
public class InvoiceRepository {

    private static final String[] MONTHS_BG = {
            "Яну", "Фев", "Мар", "Апр", "Май", "Юни",
            "Юли", "Авг", "Сеп", "Окт", "Ное", "Дек"
    };

    private final InvoiceEntityRepository invoiceEntityRepository;
    private final UserEntityRepository userEntityRepository;

    public InvoiceRepository(InvoiceEntityRepository invoiceEntityRepository,
                             UserEntityRepository userEntityRepository) {
        this.invoiceEntityRepository = invoiceEntityRepository;
        this.userEntityRepository = userEntityRepository;
    }

    public void save(Invoice invoice, String reference, String period) {
        UserEntity userEntity = userEntityRepository.findByReference(reference);

        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setDateTime(invoice.documentDate());
        entity.setNumber(invoice.documentNumber());
        entity.setUser(userEntity);
        entity.setTotalAmount(BigDecimal.valueOf(invoice.totalAmount()));
        entity.setPeriod(period);
        entity.setPaid(false);

        for (InvoiceLine line : invoice.lines()) {
            LineEntity lineEntity = new LineEntity();
            lineEntity.setId(UUID.randomUUID().toString().replace("-", ""));
            lineEntity.setLineId(line.index());
            lineEntity.setQuantity(BigDecimal.valueOf(line.quantity()));
            lineEntity.setStartDateTime(line.lineStart());
            lineEntity.setEndDateTime(line.lineEnd());
            lineEntity.setProduct(ProductType.valueOf(line.product()));
            lineEntity.setPrice(BigDecimal.valueOf(line.price()));
            lineEntity.setPriceList(line.priceList());
            lineEntity.setAmount(BigDecimal.valueOf(line.amount()));
            lineEntity.setInvoice(entity);
            entity.getLines().add(lineEntity);
        }

        invoiceEntityRepository.save(entity);
    }

    public Invoice findByUserReferenceAndPeriod(String reference, String period) {
        InvoiceEntity entity = invoiceEntityRepository.findByUserReferenceAndPeriod(reference, period);
        if (entity == null) return null;
        return toDomain(entity);
    }

    public List<InvoiceSummary> findAllSummaries() {
        return invoiceEntityRepository.findAll()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public List<InvoiceSummary> findSummariesForUser(String reference) {
        if (reference == null) return List.of();
        return findSummariesByReference(reference);
    }

    public List<InvoiceSummary> findSummariesByReference(String reference) {
        return invoiceEntityRepository.findByUserReference(reference)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private InvoiceSummary toSummary(InvoiceEntity entity) {
        return new InvoiceSummary(
                "INV-" + entity.getDateTime().getYear() + "-" + entity.getNumber(),
                "CUS-" + entity.getUser().getReference(),
                entity.getUser().getName(),
                formatPeriod(entity.getPeriod()),
                formatAmount(entity.getTotalAmount()),
                "Generated"
        );
    }

    private String formatPeriod(String period) {
        String startPart = period.split("_")[0];
        LocalDate start = LocalDate.parse(startPart);
        return MONTHS_BG[start.getMonthValue() - 1] + " " + start.getYear();
    }

    private String formatAmount(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("bg"));
        symbols.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return format.format(amount) + " лв.";
    }

    public Invoice toDomain(InvoiceEntity entity) {
        List<InvoiceLine> lines = entity.getLines().stream()
                .map(l -> new InvoiceLine(
                        l.getLineId(),
                        l.getQuantity().doubleValue(),
                        l.getStartDateTime(),
                        l.getEndDateTime(),
                        l.getProduct().name(),
                        l.getPrice().doubleValue(),
                        l.getPriceList(),
                        l.getAmount().doubleValue()
                ))
                .toList();

        return new Invoice(
                entity.getDateTime(),
                entity.getNumber(),
                entity.getUser().getName(),
                entity.getUser().getReference(),
                entity.getTotalAmount().doubleValue(),
                lines
        );
    }
}
