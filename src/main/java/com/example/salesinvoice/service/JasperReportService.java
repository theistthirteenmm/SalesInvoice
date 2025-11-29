package com.example.salesinvoice.service;

import com.example.salesinvoice.entity.Invoice;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class JasperReportService {

    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
        // آدرس فایل jrxml template
        InputStream reportStream = getClass()
                .getResourceAsStream("/reports/invoice_template.jrxml");

        JasperReport jasperReport = JasperCompileManager
                .compileReport(reportStream);

        // پارامترها
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invoiceTitle", invoice.getTitle());
        parameters.put("invoiceDate", invoice.getInvoiceDate().toString());
        parameters.put("totalAmount", invoice.getTotalAmount());

        if (invoice.getLogo() != null) {
            ByteArrayInputStream logoStream =
                    new ByteArrayInputStream(invoice.getLogo());
            parameters.put("logo", logoStream);
        }

        // DataSource برای items
        JRBeanCollectionDataSource itemsDataSource =
                new JRBeanCollectionDataSource(invoice.getItems());

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, itemsDataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}