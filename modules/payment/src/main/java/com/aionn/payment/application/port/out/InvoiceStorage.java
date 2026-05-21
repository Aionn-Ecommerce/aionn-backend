package com.aionn.payment.application.port.out;

/**
 * Storage for generated invoice PDFs. The default impl simply returns a
 * deterministic URL pointing at an internal endpoint; remote impl would
 * upload to S3/GCS.
 */
public interface InvoiceStorage {
    String storeInvoice(String paymentId, String orderId, byte[] pdfBytes);

    /** Convenience for impls that generate the PDF themselves. */
    String storeInvoiceUrl(String paymentId, String orderId);
}

