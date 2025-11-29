package com.example.salesinvoice.service;


import com.example.salesinvoice.dto.InvoiceDTO;
import com.example.salesinvoice.entity.Invoice;
import com.example.salesinvoice.entity.InvoiceItem;
import com.example.salesinvoice.entity.User;
import com.example.salesinvoice.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public Invoice createInvoice(InvoiceDTO dto, User user) {
        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setTitle(dto.getTitle());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setLogo(dto.getLogo());

        List<InvoiceItem> items = dto.getItems().stream()
                .map(itemDto -> {
                    InvoiceItem item = new InvoiceItem();
                    item.setInvoice(invoice);
                    item.setItemName(itemDto.getItemName());
                    item.setQuantity(itemDto.getQuantity());
                    item.setUnitPrice(itemDto.getUnitPrice());
                    item.setTotalPrice(itemDto.getUnitPrice()
                            .multiply(BigDecimal.valueOf(itemDto.getQuantity())));
                    return item;
                }).collect(Collectors.toList());

        invoice.setItems(items);
        invoice.setTotalAmount(items.stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return invoiceRepository.save(invoice);
    }

    // متد جدید برای پیدا کردن فاکتور با ID
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    // متدهای دیگر که ممکنه نیاز داشته باشی
    public List<Invoice> findAllByUser(User user) {
        return invoiceRepository.findByUserId(user.getId());
    }
}