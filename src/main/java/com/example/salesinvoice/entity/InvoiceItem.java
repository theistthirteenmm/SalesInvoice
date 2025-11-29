package com.example.salesinvoice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private String itemName; // نام کالا

    private Integer quantity; // تعداد

    private BigDecimal unitPrice; // قیمت واحد

    private BigDecimal totalPrice; // قیمت کل
}