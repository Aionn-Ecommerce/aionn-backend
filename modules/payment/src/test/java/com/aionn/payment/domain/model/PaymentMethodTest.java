package com.aionn.payment.domain.model;

import com.aionn.payment.domain.event.PaymentMethodEvents;
import com.aionn.payment.domain.exception.PaymentErrorCode;
import com.aionn.payment.domain.exception.PaymentException;
import com.aionn.payment.domain.valueobject.PaymentMethodStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentMethodTest {

    @Test
    void linkInitializesAsLinkedAndEmitsEvent() {
        PaymentMethod m = PaymentMethod.link("m1", "user-1", "stripe", "4242", "tok-abc");

        assertThat(m.getStatus()).isEqualTo(PaymentMethodStatus.LINKED);
        assertThat(m.peekEvents()).anyMatch(env -> env.payload() instanceof PaymentMethodEvents.PaymentMethodLinked);
    }

    @Test
    void linkRejectsBlankToken() {
        assertThatThrownBy(() -> PaymentMethod.link("m1", "user-1", "stripe", "4242", " "))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.INVALID_ARGUMENT.getCode());
    }

    @Test
    void verifyTransitionsToVerified() {
        PaymentMethod m = PaymentMethod.link("m1", "user-1", "stripe", "4242", "tok-abc");
        m.pullEvents();

        m.verify();

        assertThat(m.getStatus()).isEqualTo(PaymentMethodStatus.VERIFIED);
        assertThat(m.getVerifiedAt()).isNotNull();
        assertThat(m.peekEvents()).anyMatch(env -> env.payload() instanceof PaymentMethodEvents.PaymentMethodVerified);
    }

    @Test
    void removeIsIdempotent() {
        PaymentMethod m = PaymentMethod.link("m1", "user-1", "stripe", "4242", "tok-abc");
        m.remove();
        m.pullEvents();

        m.remove();

        assertThat(m.getStatus()).isEqualTo(PaymentMethodStatus.REMOVED);
        assertThat(m.peekEvents()).isEmpty();
    }

    @Test
    void verifyAfterRemovedThrows() {
        PaymentMethod m = PaymentMethod.link("m1", "user-1", "stripe", "4242", "tok-abc");
        m.remove();

        assertThatThrownBy(m::verify)
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_INVALID_STATE.getCode());
    }

    @Test
    void ensureOwnedByOtherUserForbidden() {
        PaymentMethod m = PaymentMethod.link("m1", "user-1", "stripe", "4242", "tok-abc");

        assertThatThrownBy(() -> m.ensureOwnedBy("OTHER"))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.METHOD_FORBIDDEN.getCode());
    }
}
