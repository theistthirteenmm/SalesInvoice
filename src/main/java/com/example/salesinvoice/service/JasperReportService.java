package com.example.salesinvoice.service;


import com.example.salesinvoice.entity.Invoice;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class JasperReportService {

    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {

        System.out.println("🔄 شروع تولید PDF برای فاکتور ID: " + invoice.getId());

        // آدرس فایل jrxml template
        InputStream reportStream = getClass()
                .getResourceAsStream("/reports/invoice_template.jrxml");

        if (reportStream == null) {
            throw new RuntimeException("فایل قالب فاکتور یافت نشد!");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // پارامترها
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invoiceTitle", invoice.getTitle());

        // تبدیل تاریخ میلادی به شمسی (ساده)
        parameters.put("invoiceDate", convertToJalali(invoice.getInvoiceDate()));

        // فرمت کردن مبلغ با جداکننده هزارگان
        NumberFormat formatter = NumberFormat.getInstance(new Locale("fa", "IR"));
        parameters.put("totalAmount", invoice.getTotalAmount());

        // لوگو
        if (invoice.getLogo() != null && invoice.getLogo().length > 0) {
            ByteArrayInputStream logoStream = new ByteArrayInputStream(invoice.getLogo());
            parameters.put("logo", logoStream);
        } else {
            parameters.put("logo", null);
        }

        System.out.println("   عنوان: " + invoice.getTitle());
        System.out.println("   تعداد اقلام: " + invoice.getItems().size());
        System.out.println("   مبلغ کل: " + invoice.getTotalAmount());

        // DataSource برای items
        JRBeanCollectionDataSource itemsDataSource =
                new JRBeanCollectionDataSource(invoice.getItems());

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, itemsDataSource);

        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        System.out.println("✅ PDF با موفقیت تولید شد - حجم: " + pdfBytes.length + " بایت");

        return pdfBytes;
    }

    // تبدیل ساده تاریخ میلادی به شمسی
    private String convertToJalali(java.time.LocalDate gregorianDate) {
        if (gregorianDate == null) {
            return "";
        }

        try {
            int gy = gregorianDate.getYear();
            int gm = gregorianDate.getMonthValue();
            int gd = gregorianDate.getDayOfMonth();

            int[] jalali = gregorianToJalali(gy, gm, gd);

            return String.format("%04d/%02d/%02d", jalali[0], jalali[1], jalali[2]);
        } catch (Exception e) {
            return gregorianDate.toString();
        }
    }

    // الگوریتم تبدیل میلادی به شمسی
    private int[] gregorianToJalali(int gy, int gm, int gd) {
        int[] g_d_m = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};

        if (gy > 1600) {
            int jy = 979;
            gy -= 1600;
        } else {
            int jy = 0;
            gy -= 621;
        }

        int gy2 = (gm > 2) ? (gy + 1) : gy;
        int days = (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) - 80 + gd + g_d_m[gm - 1];
        int jy = -1595 + (33 * (days / 12053));
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;

        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }

        int jm, jd;
        if (days < 186) {
            jm = 1 + days / 31;
            jd = 1 + (days % 31);
        } else {
            jm = 7 + (days - 186) / 30;
            jd = 1 + ((days - 186) % 30);
        }

        return new int[]{jy, jm, jd};
    }
}