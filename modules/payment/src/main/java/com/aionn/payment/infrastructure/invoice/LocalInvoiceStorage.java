package com.aionn.payment.infrastructure.invoice;

import com.aionn.payment.application.port.out.InvoiceStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default invoice storage. Returns a deterministic local URL pointing at an
 * application endpoint that should serve the invoice PDF (PDF generation is
 * out of scope for this iteration).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "payment.invoice", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalInvoiceStorage implements InvoiceStorage {

    @Value("${payment.invoice.base-url:https://invoices.test/}")
    private String baseUrl;

    @Override
    public String storeInvoice(String paymentId, String orderId, byte[] pdfBytes) {
        log.debug("[INVOICE] storing {} bytes for payment={}", pdfBytes == null ? 0 : pdfBytes.length, paymentId);
        return storeInvoiceUrl(paymentId, orderId);
    }

    @Override
    public String storeInvoiceUrl(String paymentId, String orderId) {
        String url = baseUrl;
        if (!url.endsWith("/"))
            url += "/";
        return url + "invoices/" + paymentId + ".pdf";
    }
}

