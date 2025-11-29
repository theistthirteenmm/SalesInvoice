package com.example.salesinvoice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.salesinvoice.dto.InvoiceDTO;
import com.example.salesinvoice.entity.Invoice;
import com.example.salesinvoice.entity.User;
import com.example.salesinvoice.service.InvoiceService;
import com.example.salesinvoice.service.JasperReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createInvoice(
            @RequestPart("invoice") String invoiceJson,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            Authentication authentication) {

        try {
            // دریافت User از Authentication
            User user = (User) authentication.getPrincipal();

            // تبدیل JSON به DTO
            InvoiceDTO invoiceDto = objectMapper.readValue(invoiceJson, InvoiceDTO.class);

            // اگر لوگو آپلود شده بود
            if (logo != null && !logo.isEmpty()) {
                invoiceDto.setLogo(logo.getBytes());
            }

            // ایجاد فاکتور
            Invoice invoice = invoiceService.createInvoice(invoiceDto, user);

            System.out.println("✅ فاکتور با موفقیت ایجاد شد - ID: " + invoice.getId());

            return ResponseEntity.ok(invoice);

        } catch (IOException e) {
            System.err.println("❌ خطا در پردازش داده‌ها: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("خطا در پردازش داده‌ها: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ خطای غیرمنتظره: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("خطای سرور: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            // دریافت فاکتور
            Invoice invoice = invoiceService.findById(id);

            // بررسی دسترسی کاربر
            User user = (User) authentication.getPrincipal();
            if (!invoice.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).build();
            }

            // تولید PDF
            byte[] pdfBytes = reportService.generateInvoicePdf(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("invoice_" + id + ".pdf")
                            .build()
            );

            System.out.println("✅ PDF با موفقیت تولید شد - Invoice ID: " + id);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            System.err.println("❌ خطا در تولید PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}