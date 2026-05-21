package com.aionn.payment.infrastructure.invoice;

import com.aionn.payment.application.port.out.InvoiceStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "payment.invoice", name = "provider", havingValue = "remote")
public class RemoteInvoiceStorage implements InvoiceStorage {

    @Override
    public String storeInvoice(String paymentId, String orderId, byte[] pdfBytes) {
        throw new UnsupportedOperationException("Remote InvoiceStorage is not implemented yet");
    }

    @Override
    public String storeInvoiceUrl(String paymentId, String orderId) {
        throw new UnsupportedOperationException("Remote InvoiceStorage is not implemented yet");
    }
}

