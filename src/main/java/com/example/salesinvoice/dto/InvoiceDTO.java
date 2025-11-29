package com.example.salesinvoice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceDTO {
    private String title;
    private LocalDate invoiceDate;
    private byte[] logo;
    private List<InvoiceItemDTO> items;
}