package com.aionn.ucp.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ucp_cart_sessions", indexes = {
        @Index(name = "idx_cart_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSessionEntity {

    @Id
    @Column(name = "cart_id", length = 50)
    private String cartId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "line_items_json", columnDefinition = "TEXT", nullable = false)
    private String lineItemsJson;

    @Column(name = "totals_json", columnDefinition = "TEXT")
    private String totalsJson;

    @Column(name = "continue_url", length = 500)
    private String continueUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
