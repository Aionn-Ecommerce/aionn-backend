package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.application.service.StripeConnectService;
import com.aionn.payment.infrastructure.provider.config.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/webhooks/stripe-connect")
@RequiredArgsConstructor
@Tag(name = "Payment - Stripe Connect Webhook", description = "Stripe Connect account capability updates")
public class StripeConnectWebhookController {

    private final StripeConnectService stripeConnectService;
    private final StripeProperties stripeProperties;

    @PostMapping
    @Operation(summary = "Stripe Connect webhook (account.updated)")
    public ResponseEntity<Void> handle(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody String rawBody) {
        if (signature == null || stripeProperties.webhookSecret() == null
                || stripeProperties.webhookSecret().isBlank()) {
            log.warn("Stripe Connect webhook missing signature or secret not configured");
            return ResponseEntity.badRequest().build();
        }
        Event event;
        try {
            event = Webhook.constructEvent(rawBody, signature, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException ex) {
            log.warn("Stripe Connect webhook signature mismatch: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
        if (!"account.updated".equals(event.getType())) {
            return ResponseEntity.ok().build();
        }
        Account account = event.getDataObjectDeserializer().getObject()
                .filter(Account.class::isInstance)
                .map(Account.class::cast)
                .orElse(null);
        if (account == null) {
            log.warn("Stripe Connect webhook payload could not be parsed");
            return ResponseEntity.badRequest().build();
        }
        stripeConnectService.applyAccountUpdate(account);
        return ResponseEntity.ok().build();
    }
}
