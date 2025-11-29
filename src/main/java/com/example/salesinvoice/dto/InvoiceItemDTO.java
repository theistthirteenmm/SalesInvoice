package com.example.salesinvoice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemDTO {
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
}