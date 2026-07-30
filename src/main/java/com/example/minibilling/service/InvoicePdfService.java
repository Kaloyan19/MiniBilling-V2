package com.example.minibilling.service;

import com.example.minibilling.model.entity.InvoiceEntity;
import com.example.minibilling.model.entity.LineEntity;
import com.example.minibilling.repository.jpa.InvoiceEntityRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

@Service
public class InvoicePdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd/yy");

    private static final Font TITLE = new Font(Font.HELVETICA, 42, Font.BOLD);
    private static final Font BOLD = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 10);
    private static final Font HEADER_WHITE = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

    private final InvoiceEntityRepository invoiceRepository;

    public InvoicePdfService(InvoiceEntityRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public byte[] generatePdf(String invoiceId) {
        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .or(() -> findByDisplayId(invoiceId))
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceId));
        return render(invoice);
    }

    // The invoice list endpoint exposes display ids of the form "INV-{year}-{number}"
    private java.util.Optional<InvoiceEntity> findByDisplayId(String displayId) {
        if (!displayId.startsWith("INV-")) {
            return java.util.Optional.empty();
        }
        String[] parts = displayId.split("-", 3);
        if (parts.length < 3) {
            return java.util.Optional.empty();
        }
        return invoiceRepository.findFirstByNumber(parts[2]);
    }

    private byte[] render(InvoiceEntity invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("INVOICE", TITLE));
        document.add(Chunk.NEWLINE);

        document.add(buildHeader(invoice));
        document.add(Chunk.NEWLINE);

        document.add(buildLinesTable(invoice));
        document.add(Chunk.NEWLINE);

        document.add(buildTotals(invoice));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Notes:", BOLD));
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Terms & Conditions:", BOLD));

        Paragraph thanks = new Paragraph("THANK YOU", new Font(Font.HELVETICA, 16, Font.BOLD));
        thanks.setAlignment(Element.ALIGN_CENTER);
        thanks.setSpacingBefore(40);
        document.add(thanks);

        document.close();
        return out.toByteArray();
    }

    private PdfPTable buildHeader(InvoiceEntity invoice) {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{55, 45});

        PdfPTable meta = new PdfPTable(2);
        meta.setWidths(new float[]{40, 60});
        addMetaRow(meta, "Invoice #:", invoice.getNumber());
        addMetaRow(meta, "Invoice Date:", invoice.getDateTime() != null
                ? invoice.getDateTime().format(DATE) : "");
        addMetaRow(meta, "Invoice Period:", invoice.getPeriod());
        addMetaRow(meta, "Status:", invoice.isPaid() ? "Paid" : "Unpaid");

        PdfPTable billTo = new PdfPTable(1);
        PdfPCell title = cell(new Phrase("Bill To:", BOLD));
        billTo.addCell(title);
        billTo.addCell(cell(new Phrase(invoice.getUser() != null ? invoice.getUser().getName() : "", NORMAL)));
        billTo.addCell(cell(new Phrase("Ref: " + (invoice.getUser() != null ? invoice.getUser().getReference() : ""), NORMAL)));

        header.addCell(wrap(meta));
        header.addCell(wrap(billTo));
        return header;
    }

    private PdfPTable buildLinesTable(InvoiceEntity invoice) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{22, 30, 14, 14, 20});

        for (String h : new String[]{"Product", "Period", "Price", "Quantity", "Total"}) {
            PdfPCell head = new PdfPCell(new Phrase(h, HEADER_WHITE));
            head.setBackgroundColor(Color.BLACK);
            head.setPadding(6);
            table.addCell(head);
        }

        for (LineEntity line : invoice.getLines()) {
            table.addCell(lineCell(line.getProduct() != null ? line.getProduct().name() : "", Element.ALIGN_LEFT));
            String period = format(line.getStartDateTime()) + "-" + format(line.getEndDateTime());
            table.addCell(lineCell(period, Element.ALIGN_LEFT));
            table.addCell(lineCell(money(line.getPrice()), Element.ALIGN_RIGHT));
            table.addCell(lineCell(plain(line.getQuantity()), Element.ALIGN_RIGHT));
            table.addCell(lineCell(money(line.getAmount()), Element.ALIGN_RIGHT));
        }
        return table;
    }

    private PdfPTable buildTotals(InvoiceEntity invoice) {
        BigDecimal subtotal = invoice.getLines().stream()
                .map(LineEntity::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : subtotal;
        BigDecimal vat = grandTotal.subtract(subtotal);

        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(40);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

        totals.addCell(borderedCell(new Phrase("Subtotal:", NORMAL), null));
        totals.addCell(borderedCell(new Phrase(money(subtotal), NORMAL), null));
        totals.addCell(borderedCell(new Phrase("VAT", NORMAL), null));
        totals.addCell(borderedCell(new Phrase(money(vat), NORMAL), null));
        totals.addCell(borderedCell(new Phrase("Grand Total", HEADER_WHITE), Color.BLACK));
        totals.addCell(borderedCell(new Phrase(money(grandTotal), HEADER_WHITE), Color.BLACK));
        return totals;
    }

    private void addMetaRow(PdfPTable table, String label, String value) {
        table.addCell(cell(new Phrase(label, NORMAL)));
        table.addCell(cell(new Phrase(value != null ? value : "", NORMAL)));
    }

    private PdfPCell cell(Phrase phrase) {
        PdfPCell c = new PdfPCell(phrase);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(3);
        return c;
    }

    private PdfPCell wrap(PdfPTable inner) {
        PdfPCell c = new PdfPCell(inner);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell lineCell(String text, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, NORMAL));
        c.setPadding(5);
        c.setHorizontalAlignment(align);
        c.setBorder(Rectangle.BOTTOM);
        return c;
    }

    private PdfPCell borderedCell(Phrase phrase, Color background) {
        PdfPCell c = new PdfPCell(phrase);
        c.setPadding(5);
        if (background != null) {
            c.setBackgroundColor(background);
        }
        return c;
    }

    private String format(java.time.OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE) : "";
    }

    private String money(BigDecimal value) {
        return value != null ? "€ " + value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "";
    }

    private String plain(BigDecimal value) {
        return value != null ? value.stripTrailingZeros().toPlainString() : "";
    }
}
