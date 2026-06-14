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
@Table(name = "ucp_order_event_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventOutboxEntity {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "order_id", length = 64, nullable = false)
    private String orderId;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "webhook_url", columnDefinition = "TEXT", nullable = false)
    private String webhookUrl;

    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb", nullable = false)
    private String payloadJson;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;
}
