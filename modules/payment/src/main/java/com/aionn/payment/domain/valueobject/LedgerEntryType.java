package com.aionn.payment.domain.valueobject;

public enum LedgerEntryType {
    /** Money flowing INTO the system (customer charge). */
    CREDIT,
    /** Money flowing OUT of the system (refund / payout). */
    DEBIT
}

