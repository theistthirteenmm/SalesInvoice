package com.example.salesinvoice.controller;

import com.example.salesinvoice.entity.Invoice;
import com.example.salesinvoice.service.InvoiceService;
import com.example.salesinvoice.service.JasperReportService;
import com.example.salesinvoice.entity.User;
import com.example.salesinvoice.dto.InvoiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "http://localhost:3000")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private JasperReportService reportService;

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(
            @RequestPart("invoice") InvoiceDTO invoiceDto,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @AuthenticationPrincipal User user) throws IOException {

        if (logo != null) {
            invoiceDto.setLogo(logo.getBytes());
        }

        Invoice invoice = invoiceService.createInvoice(invoiceDto, user);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceService.findById(id);
            byte[] pdfBytes = reportService.generateInvoicePdf(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("invoice_" + id + ".pdf")
                            .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}