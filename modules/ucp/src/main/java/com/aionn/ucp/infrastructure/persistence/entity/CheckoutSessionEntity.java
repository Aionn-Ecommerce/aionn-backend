package com.aionn.ucp.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "ucp_checkout_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionEntity {

    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "platform_profile_url", columnDefinition = "TEXT")
    private String platformProfileUrl;

    @Column(name = "webhook_url", columnDefinition = "TEXT")
    private String webhookUrl;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "currency", length = 8, nullable = false)
    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "line_items_json", columnDefinition = "jsonb", nullable = false)
    private String lineItemsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "totals_json", columnDefinition = "jsonb", nullable = false)
    private String totalsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "discounts_json", columnDefinition = "jsonb")
    private String discountsJson;

    @Column(name = "order_id", length = 64)
    private String orderId;

    @Column(name = "cart_id", length = 64)
    private String cartId;

    @Column(name = "continue_url", columnDefinition = "TEXT")
    private String continueUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
