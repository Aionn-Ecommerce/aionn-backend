package com.aionn.payment.domain.model;

import com.aionn.payment.domain.event.LedgerEvents;
import com.aionn.payment.domain.valueobject.LedgerEntryType;
import com.aionn.sharedkernel.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionLedgerTest {

    @Test
    void recordCreatesEntryWithEvent() {
        TransactionLedger l = TransactionLedger.record(
                "led-1", "p-1", Money.of(new BigDecimal("100"), "VND"),
                LedgerEntryType.CREDIT, "MOCK", "txn-1");

        assertThat(l.getLedgerId()).isEqualTo("led-1");
        assertThat(l.getType()).isEqualTo(LedgerEntryType.CREDIT);
        assertThat(l.getOccurredAt()).isNotNull();
        assertThat(l.peekEvents()).anyMatch(env -> env.payload() instanceof LedgerEvents.LedgerEntryRecorded);
    }
}
